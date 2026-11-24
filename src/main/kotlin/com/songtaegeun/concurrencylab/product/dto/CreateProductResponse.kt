package com.songtaegeun.concurrencylab.product.dto

import com.songtaegeun.concurrencylab.product.domain.Product

data class CreateProductResponse(
    val id: Long,
    val name: String,
    val price: Long,
    val stockQuantity: Int,
    val deliveryLeadTimeDays: Int,
) {
    companion object {
        fun from(
            product: Product,
            stockQuantity: Int,
            deliveryLeadTimeDays: Int,
        ): CreateProductResponse = CreateProductResponse(
            id = requireNotNull(product.id),
            name = product.name,
            price = product.price,
            stockQuantity = stockQuantity,
            deliveryLeadTimeDays = deliveryLeadTimeDays,
        )
    }
}
