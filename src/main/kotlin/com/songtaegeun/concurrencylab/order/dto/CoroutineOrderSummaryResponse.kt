package com.songtaegeun.concurrencylab.order.dto

data class CoroutineOrderSummaryResponse(
    val product: OrderSummaryProductResponse,
    val inventory: OrderSummaryInventoryResponse,
    val delivery: OrderSummaryDeliveryResponse,
    val processingTimeMillis: Long,
    val threadNames: OrderSummaryThreadNamesResponse,
)
