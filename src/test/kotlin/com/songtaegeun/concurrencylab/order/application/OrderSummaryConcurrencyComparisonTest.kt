package com.songtaegeun.concurrencylab.order.application

import com.songtaegeun.concurrencylab.product.application.ProductService
import com.songtaegeun.concurrencylab.product.dto.CreateProductRequest
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.TimeSource

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "lab.blocking-delay=500ms",
        // 네 방식 모두 동일하게 최대 30개의 HTTP 요청을 동시에 받도록 고정한다.
        "server.tomcat.threads.max=30",
        "server.tomcat.threads.min-spare=30",
        // 30개 요청 x Reader 3개가 DB 커넥션 풀 때문에 먼저 제한되지 않도록 한다.
        "spring.datasource.hikari.maximum-pool-size=100",
    ],
)
@ActiveProfiles("test")
class OrderSummaryConcurrencyComparisonTest @Autowired constructor(
    private val productService: ProductService,
    private val dataSource: DataSource,
) {
    @LocalServerPort
    private var port: Int = 0

    private var productId: Long = 0L

    // 테스트 클라이언트도 30개의 동일한 플랫폼 스레드에서 HTTP 요청을 발생시킨다.
    private val requestExecutor: ExecutorService = Executors.newFixedThreadPool(
        REQUEST_COUNT,
        Thread.ofPlatform()
            .name("comparison-client-", 0)
            .factory(),
    )
    private val httpClient: HttpClient = HttpClient.newBuilder().build()

    @BeforeEach
    fun setUp() {
        productId = productService.create(
            CreateProductRequest(
                name = "동시성 비교용 상품",
                price = 10_000L,
                stockQuantity = 100,
                deliveryLeadTimeDays = 2,
            ),
        ).id
    }

    @AfterEach
    fun tearDown() {
        requestExecutor.shutdown()
    }

    @Test
    fun `네 가지 주문 요약 API에 각각 30개의 요청을 동시에 보내 처리 시간을 비교한다`() {
        // 최초 HTTP 연결, 클래스 로딩, JPA 초기화 비용이 측정값에 섞이지 않도록 워밍업한다.
        ComparisonTarget.entries.forEach(::warmUp)

        // 다른 방식과 자원을 경쟁하지 않도록 각 방식을 순서대로 단독 측정한다.
        val measurements = ComparisonTarget.entries.associateWith(::measureConcurrentRequests)

        measurements.values.forEach { measurement ->
            assertEquals(REQUEST_COUNT, measurement.completedRequests)
        }

        val hikariMaximumPoolSize = (dataSource as HikariDataSource).maximumPoolSize
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        val coroutineIoParallelism = System.getProperty(COROUTINE_IO_PARALLELISM_PROPERTY)
            ?.toIntOrNull()
            ?: maxOf(DEFAULT_COROUTINE_IO_PARALLELISM, availableProcessors)

        println(
            """
            |=== 주문 요약 API별 ${REQUEST_COUNT}개 동시 요청 비교 ===
            |availableProcessors=$availableProcessors
            |tomcatMaxThreads=$REQUEST_COUNT
            |clientRequestThreads=$REQUEST_COUNT
            |hikariMaximumPoolSize=$hikariMaximumPoolSize
            |coroutineIoParallelism=$coroutineIoParallelism
            |forkJoinParallelism=${ForkJoinPool.commonPool().parallelism}
            |
            |${measurements.toDisplayText()}
            """.trimMargin(),
        )
    }

    /**
     * 같은 크기의 클라이언트 플랫폼 스레드 풀에 요청 30개를 준비한 뒤,
     * startGate를 한 번에 열어 네 API에 동일한 형태로 동시 요청을 보낸다.
     */
    private fun measureConcurrentRequests(target: ComparisonTarget): BatchMeasurement {
        val readyGate = CountDownLatch(REQUEST_COUNT)
        val startGate = CountDownLatch(1)

        val requests = List(REQUEST_COUNT) {
            CompletableFuture.supplyAsync(
                {
                    readyGate.countDown()
                    startGate.await()
                    sendRequest(target)
                },
                requestExecutor,
            )
        }

        assertTrue(
            readyGate.await(READY_TIMEOUT_SECONDS, TimeUnit.SECONDS),
            "${REQUEST_COUNT}개의 HTTP 요청이 제한 시간 안에 준비되지 않았습니다.",
        )

        val startedAt = TimeSource.Monotonic.markNow()
        startGate.countDown()

        CompletableFuture.allOf(*requests.toTypedArray()).join()
        val responses = requests.map { it.join() }

        responses.forEach { response ->
            assertEquals(200, response.statusCode(), response.body())
        }

        return BatchMeasurement(
            elapsedMillis = startedAt.elapsedNow().inWholeMilliseconds,
            completedRequests = responses.size,
            serverProcessingMillis = responses.map { response ->
                PROCESSING_TIME_PATTERN.find(response.body())
                    ?.groupValues
                    ?.get(1)
                    ?.toLong()
                    ?: error("processingTimeMillis가 응답에 없습니다: ${response.body()}")
            },
            serverThreadNames = responses
                .flatMap { response ->
                    THREAD_NAME_PATTERN.findAll(response.body())
                        .map { match -> match.groupValues[1].substringBefore(" @coroutine#") }
                        .toList()
                }
                .toSet(),
        )
    }

    private fun warmUp(target: ComparisonTarget) {
        val response = sendRequest(target)
        assertEquals(200, response.statusCode(), response.body())
    }

    private fun sendRequest(target: ComparisonTarget): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port/api/v1/order-summaries/${target.path}/$productId"))
            .GET()
            .build()

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun Map<ComparisonTarget, BatchMeasurement>.toDisplayText(): String =
        entries.joinToString(separator = "\n\n") { (target, measurement) ->
            """
            |[${target.displayName}]
            |batchElapsed=${measurement.elapsedMillis}ms
            |completedRequests=${measurement.completedRequests}
            |averageServerProcessing=${"%.1f".format(measurement.serverProcessingMillis.average())}ms
            |serverThreadCount=${measurement.serverThreadNames.size}
            |serverThreadNames=${measurement.serverThreadNames}
            """.trimMargin()
        }

    private enum class ComparisonTarget(
        val displayName: String,
        val path: String,
    ) {
        BLOCKING("일반 Blocking", "blocking"),
        COROUTINE("Coroutine + Blocking I/O", "coroutine"),
        VIRTUAL_THREAD("Virtual Thread + Blocking I/O", "virtual-thread"),
        FORK_JOIN("ForkJoinPool + Blocking I/O", "fork-join"),
    }

    private data class BatchMeasurement(
        val elapsedMillis: Long,
        val completedRequests: Int,
        val serverProcessingMillis: List<Long>,
        val serverThreadNames: Set<String>,
    )

    private companion object {
        const val REQUEST_COUNT = 30
        const val READY_TIMEOUT_SECONDS = 5L
        const val DEFAULT_COROUTINE_IO_PARALLELISM = 64
        const val COROUTINE_IO_PARALLELISM_PROPERTY = "kotlinx.coroutines.io.parallelism"

        val PROCESSING_TIME_PATTERN = Regex("\\\"processingTimeMillis\\\"\\s*:\\s*(\\d+)")
        val THREAD_NAME_PATTERN = Regex(
            "\\\"(?:threadName|product|inventory|delivery)\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"",
        )
    }
}
