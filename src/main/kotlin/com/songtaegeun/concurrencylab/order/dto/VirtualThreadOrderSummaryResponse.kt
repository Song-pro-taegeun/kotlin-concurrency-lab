package com.songtaegeun.concurrencylab.order.dto

data class VirtualThreadOrderSummaryResponse(
    val product: OrderSummaryProductResponse,
    val inventory: OrderSummaryInventoryResponse,
    val delivery: OrderSummaryDeliveryResponse,
    val processingTimeMillis: Long,
    val threadNames: OrderSummaryThreadNamesResponse,
)
