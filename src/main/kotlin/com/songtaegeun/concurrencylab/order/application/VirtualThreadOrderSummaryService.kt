package com.songtaegeun.concurrencylab.order.application

import com.songtaegeun.concurrencylab.delivery.application.BlockingDeliveryReader
import com.songtaegeun.concurrencylab.inventory.application.BlockingInventoryReader
import com.songtaegeun.concurrencylab.order.application.model.OrderSummaryExecutionType
import com.songtaegeun.concurrencylab.order.application.model.OrderSummaryReadResult
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryDeliveryResponse
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryInventoryResponse
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryProductResponse
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryThreadNamesResponse
import com.songtaegeun.concurrencylab.order.dto.VirtualThreadOrderSummaryResponse
import com.songtaegeun.concurrencylab.product.application.BlockingProductReader
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import kotlin.time.TimeSource

@Service
class VirtualThreadOrderSummaryService(
    private val productReader: BlockingProductReader,
    private val inventoryReader: BlockingInventoryReader,
    private val deliveryReader: BlockingDeliveryReader,

    // 가상 스레드 용도
    @Qualifier("orderSummaryVirtualThreadExecutor")
    private val virtualThreadExecutor: Executor,
) {
    fun get(
        productId: Long,
        executionType: OrderSummaryExecutionType,
    ): CompletableFuture<VirtualThreadOrderSummaryResponse> {
        val startedAt = TimeSource.Monotonic.markNow()

        // 실행 방식을 나눈 이유:
        // Blocking Reader 작업을 Virtual Thread와 ForkJoinPool 플랫폼 스레드로 실행해 차이를 비교한다.
        // #참고 CompletableFuture는 Executor를 생략한 비동기 메서드는 기본적으로 ForkJoinPool.commonPool()를 사용
        //
        // - VIRTUAL_THREAD
        //   제출된 Reader 작업마다 새로운 Virtual Thread가 생성된다.
        //   Virtual Thread는 JVM의 캐리어 스레드에 mount되어 실행되고,
        //   지원되는 Blocking 대기에 들어가면 unmount되어 캐리어 스레드를 반환한다.
        //   캐리어 스케줄러는 일반적으로 전용 ForkJoinPool이며 commonPool과는 별개다.
        //
        // - FORK_JOIN
        //   Reader 작업을 ForkJoinPool.commonPool()에 제출한다.
        //   플랫폼 워커 스레드가 작업을 재사용해서 처리하며,
        //   기본 parallelism은 일반적으로 availableProcessors - 1이다.
        //   워커가 Blocking 작업으로 모두 점유되면 나머지 작업은 큐에서 대기한다.
        //
        // `가상 스레드와 ForkJoinPool로 주문 요약을 각각 10번 동시에 조회한다` 테스트 코드 실행 시 유의미한 결과를 도출
        val executor = when (executionType) {
            OrderSummaryExecutionType.VIRTUAL_THREAD -> virtualThreadExecutor
            OrderSummaryExecutionType.FORK_JOIN -> ForkJoinPool.commonPool()
        }

        val productFuture = readAsync(executor) { productReader.read(productId) }
        val inventoryFuture = readAsync(executor) { inventoryReader.read(productId) }
        val deliveryFuture = readAsync(executor) { deliveryReader.read(productId) }

        return CompletableFuture.allOf(
            productFuture,
            inventoryFuture,
            deliveryFuture,
        ).thenApply {
            val productResult = productFuture.join()
            val inventoryResult = inventoryFuture.join()
            val deliveryResult = deliveryFuture.join()

            VirtualThreadOrderSummaryResponse(
                product = OrderSummaryProductResponse(
                    id = requireNotNull(productResult.value.id),
                    name = productResult.value.name,
                    price = productResult.value.price,
                ),
                inventory = OrderSummaryInventoryResponse(
                    quantity = inventoryResult.value.quantity,
                ),
                delivery = OrderSummaryDeliveryResponse(
                    leadTimeDays = deliveryResult.value.leadTimeDays,
                    estimatedDeliveryDate = LocalDate.now()
                        .plusDays(deliveryResult.value.leadTimeDays.toLong()),
                ),
                processingTimeMillis = startedAt.elapsedNow().inWholeMilliseconds,
                threadNames = OrderSummaryThreadNamesResponse(
                    product = productResult.threadName,
                    inventory = inventoryResult.threadName,
                    delivery = deliveryResult.threadName,
                ),
            )
        }
    }

    private fun <T> readAsync(
        executor: Executor,
        reader: () -> T,
    ): CompletableFuture<OrderSummaryReadResult<T>> =
        CompletableFuture.supplyAsync(
            {
                OrderSummaryReadResult(
                    value = reader(),
                    threadName = Thread.currentThread().name,
                )
            },
            executor,
        )
}
