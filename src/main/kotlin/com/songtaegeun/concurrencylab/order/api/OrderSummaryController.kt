package com.songtaegeun.concurrencylab.order.api

import com.songtaegeun.concurrencylab.order.application.BlockingOrderSummaryService
import com.songtaegeun.concurrencylab.order.application.CoroutineOrderSummaryService
import com.songtaegeun.concurrencylab.order.application.VirtualThreadOrderSummaryService
import com.songtaegeun.concurrencylab.order.application.model.OrderSummaryExecutionType
import com.songtaegeun.concurrencylab.order.dto.BlockingOrderSummaryResponse
import com.songtaegeun.concurrencylab.order.dto.CoroutineOrderSummaryResponse
import com.songtaegeun.concurrencylab.order.dto.VirtualThreadOrderSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/api/v1/order-summaries")
class OrderSummaryController(
    private val blockingOrderSummaryService: BlockingOrderSummaryService,
    private val coroutineOrderSummaryService: CoroutineOrderSummaryService,
    private val virtualThreadOrderSummaryService: VirtualThreadOrderSummaryService,
) {
    @Operation(
        summary = "동기 방식 주문 요약 조회",
        description = "코루틴, 가상 쓰레드를 사용하지 않고 동기적으로 데이터 응답 API",
    )
    @GetMapping("/blocking/{productId}")
    fun getBlockingOrderSummary(
        @PathVariable productId: Long,
    ): BlockingOrderSummaryResponse = blockingOrderSummaryService.get(productId)

    @Operation(
        summary = "코루틴 방식 주문 요약 조회",
        description = "Blocking JPA/JDBC 조회를 Dispatchers.IO에서 코루틴으로 동시에 실행하는 API",
    )
    @GetMapping("/coroutine/{productId}")
    suspend fun getCoroutineOrderSummary(
        @PathVariable productId: Long,
    ): CoroutineOrderSummaryResponse = coroutineOrderSummaryService.get(productId)

    @Operation(
        summary = "가상 스레드 방식 주문 요약 조회",
        description = "Blocking JPA/JDBC 조회를 작업마다 생성한 Virtual Thread에서 동시에 실행하는 API",
    )
    @GetMapping("/virtual-thread/{productId}")
    fun getVirtualThreadOrderSummary(
        @PathVariable productId: Long,
    ): CompletableFuture<VirtualThreadOrderSummaryResponse> =
        virtualThreadOrderSummaryService.get(
            productId = productId,
            executionType = OrderSummaryExecutionType.VIRTUAL_THREAD,
        )

    @Operation(
        summary = "ForkJoinPool 방식 주문 요약 조회",
        description = "Blocking JPA/JDBC 조회를 ForkJoinPool.commonPool의 플랫폼 스레드에서 동시에 실행하는 API",
    )
    @GetMapping("/fork-join/{productId}")
    fun getForkJoinOrderSummary(
        @PathVariable productId: Long,
    ): CompletableFuture<VirtualThreadOrderSummaryResponse> =
        virtualThreadOrderSummaryService.get(
            productId = productId,
            executionType = OrderSummaryExecutionType.FORK_JOIN,
        )
}
