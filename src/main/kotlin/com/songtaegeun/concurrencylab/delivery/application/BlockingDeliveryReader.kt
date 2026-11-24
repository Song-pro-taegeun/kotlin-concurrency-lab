package com.songtaegeun.concurrencylab.delivery.application

import com.songtaegeun.concurrencylab.common.exception.ResourceNotFoundException
import com.songtaegeun.concurrencylab.common.support.BlockingDelay
import com.songtaegeun.concurrencylab.delivery.domain.Delivery
import com.songtaegeun.concurrencylab.delivery.domain.DeliveryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlockingDeliveryReader(
    private val deliveryRepository: DeliveryRepository,
    private val blockingDelay: BlockingDelay,
) {
    @Transactional(readOnly = true)
    fun read(productId: Long): Delivery {
        blockingDelay.simulateIo()
        return deliveryRepository.findByProduct_Id(productId)
            ?: throw ResourceNotFoundException("배송 정보를 찾을 수 없습니다. productId=$productId")
    }
}
