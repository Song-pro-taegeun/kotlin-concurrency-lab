package com.songtaegeun.concurrencylab.order.application.model

data class OrderSummaryReadResult<T>(
    val value: T,
    val threadName: String,
)
