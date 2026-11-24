package com.songtaegeun.concurrencylab.inventory.application

import com.songtaegeun.concurrencylab.common.exception.ResourceNotFoundException
import com.songtaegeun.concurrencylab.common.support.BlockingDelay
import com.songtaegeun.concurrencylab.inventory.domain.Inventory
import com.songtaegeun.concurrencylab.inventory.domain.InventoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlockingInventoryReader(
    private val inventoryRepository: InventoryRepository,
    private val blockingDelay: BlockingDelay,
) {
    @Transactional(readOnly = true)
    fun read(productId: Long): Inventory {
        blockingDelay.simulateIo()
        return inventoryRepository.findByProduct_Id(productId)
            ?: throw ResourceNotFoundException("재고를 찾을 수 없습니다. productId=$productId")
    }
}
