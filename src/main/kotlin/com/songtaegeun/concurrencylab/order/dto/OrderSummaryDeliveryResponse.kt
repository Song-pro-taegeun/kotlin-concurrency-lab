package com.songtaegeun.concurrencylab.order.dto

import java.time.LocalDate

data class OrderSummaryDeliveryResponse(
    val leadTimeDays: Int,
    val estimatedDeliveryDate: LocalDate,
)
