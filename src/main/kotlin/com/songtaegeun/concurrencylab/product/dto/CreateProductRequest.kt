package com.songtaegeun.concurrencylab.product.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero

data class CreateProductRequest(
    @field:NotBlank
    val name: String,

    @field:Positive
    val price: Long,

    @field:PositiveOrZero
    val stockQuantity: Int,

    @field:Positive
    val deliveryLeadTimeDays: Int,
)
