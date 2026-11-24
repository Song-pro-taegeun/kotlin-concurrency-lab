package com.songtaegeun.concurrencylab.product.application

import com.songtaegeun.concurrencylab.common.exception.ResourceNotFoundException
import com.songtaegeun.concurrencylab.common.support.BlockingDelay
import com.songtaegeun.concurrencylab.product.domain.Product
import com.songtaegeun.concurrencylab.product.domain.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlockingProductReader(
    private val productRepository: ProductRepository,
    private val blockingDelay: BlockingDelay,
) {
    @Transactional(readOnly = true)
    fun read(productId: Long): Product {
        blockingDelay.simulateIo()
        return productRepository.findById(productId)
            .orElseThrow { ResourceNotFoundException("상품을 찾을 수 없습니다. productId=$productId") }
    }
}
