package com.songtaegeun.concurrencylab.order.dto

data class OrderSummaryThreadNamesResponse(
    val product: String,
    val inventory: String,
    val delivery: String,
)
