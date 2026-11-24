package com.songtaegeun.concurrencylab.inventory.domain

import org.springframework.data.jpa.repository.JpaRepository

interface InventoryRepository : JpaRepository<Inventory, Long> {
    // Delivery.product.id 조회
    fun findByProduct_Id(productId: Long): Inventory?
}
