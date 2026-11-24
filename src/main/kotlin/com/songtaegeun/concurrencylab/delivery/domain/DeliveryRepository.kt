package com.songtaegeun.concurrencylab.delivery.domain

import org.springframework.data.jpa.repository.JpaRepository

interface DeliveryRepository : JpaRepository<Delivery, Long> {
    // Delivery.product.id 조회
    fun findByProduct_Id(productId: Long): Delivery?
}
