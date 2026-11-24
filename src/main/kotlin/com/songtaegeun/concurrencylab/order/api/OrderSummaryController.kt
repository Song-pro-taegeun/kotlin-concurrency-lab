package com.songtaegeun.concurrencylab.order.api

import com.songtaegeun.concurrencylab.order.application.BlockingOrderSummaryService
import com.songtaegeun.concurrencylab.order.dto.BlockingOrderSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/order-summaries")
class OrderSummaryController(
    private val blockingOrderSummaryService: BlockingOrderSummaryService,
) {
    @Operation(
        summary = "동기 방식 주문 요약 조회",
        description = "코루틴, 가상 쓰레드를 사용하지 않고 동기적으로 데이터 응답 API",
    )
    @GetMapping("/blocking/{productId}")
    fun getBlockingOrderSummary(
        @PathVariable productId: Long,
    ): BlockingOrderSummaryResponse = blockingOrderSummaryService.get(productId)
}
