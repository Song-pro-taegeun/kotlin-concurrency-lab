package com.songtaegeun.concurrencylab.order.dto

data class OrderSummaryProductResponse(
    val id: Long,
    val name: String,
    val price: Long,
)
