package com.songtaegeun.concurrencylab.order.dto

data class BlockingOrderSummaryResponse(
    val product: OrderSummaryProductResponse,
    val inventory: OrderSummaryInventoryResponse,
    val delivery: OrderSummaryDeliveryResponse,
    val processingTimeMillis: Long,
    val threadName: String,
)
