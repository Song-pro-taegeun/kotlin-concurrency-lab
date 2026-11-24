package com.songtaegeun.concurrencylab.product.application

import com.songtaegeun.concurrencylab.delivery.domain.Delivery
import com.songtaegeun.concurrencylab.delivery.domain.DeliveryRepository
import com.songtaegeun.concurrencylab.inventory.domain.Inventory
import com.songtaegeun.concurrencylab.inventory.domain.InventoryRepository
import com.songtaegeun.concurrencylab.product.domain.Product
import com.songtaegeun.concurrencylab.product.domain.ProductRepository
import com.songtaegeun.concurrencylab.product.dto.CreateProductRequest
import com.songtaegeun.concurrencylab.product.dto.CreateProductResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val deliveryRepository: DeliveryRepository,
) {
    @Transactional
    fun create(request: CreateProductRequest): CreateProductResponse {
        // 제품 생성
        val product = productRepository.save(
            Product(
                name = request.name,
                price = request.price,
            ),
        )

        // 제품 인벤토리 생성
        inventoryRepository.save(
            Inventory(
                product = product,
                quantity = request.stockQuantity,
            ),
        )

        // 상품 별 배송 예정 정보
        deliveryRepository.save(
            Delivery(
                product = product,
                leadTimeDays = request.deliveryLeadTimeDays,
            ),
        )

        return CreateProductResponse.from(
            product = product,
            stockQuantity = request.stockQuantity,
            deliveryLeadTimeDays = request.deliveryLeadTimeDays,
        )
    }
}
