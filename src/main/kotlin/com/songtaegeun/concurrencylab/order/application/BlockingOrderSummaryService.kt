package com.songtaegeun.concurrencylab.order.application

import com.songtaegeun.concurrencylab.delivery.application.BlockingDeliveryReader
import com.songtaegeun.concurrencylab.inventory.application.BlockingInventoryReader
import com.songtaegeun.concurrencylab.order.dto.BlockingOrderSummaryResponse
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryDeliveryResponse
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryInventoryResponse
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryProductResponse
import com.songtaegeun.concurrencylab.product.application.BlockingProductReader
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.time.measureTimedValue

@Service
class BlockingOrderSummaryService(
    private val productReader: BlockingProductReader,
    private val inventoryReader: BlockingInventoryReader,
    private val deliveryReader: BlockingDeliveryReader,
) {
    fun get(productId: Long): BlockingOrderSummaryResponse {
        val threadName = Thread.currentThread().name

        // measureTimedValue는 중괄호의 마지막 실행 결과값과, 해당 구문 전체 실행 시간을 반환한다.
        val (summary, duration) = measureTimedValue {
            val product = productReader.read(productId)
            val inventory = inventoryReader.read(productId)
            val delivery = deliveryReader.read(productId)

            BlockingOrderSummaryResponse(
                product = OrderSummaryProductResponse(
                    id = requireNotNull(product.id),
                    name = product.name,
                    price = product.price,
                ),
                inventory = OrderSummaryInventoryResponse(
                    quantity = inventory.quantity,
                ),
                delivery = OrderSummaryDeliveryResponse(
                    leadTimeDays = delivery.leadTimeDays,
                    estimatedDeliveryDate = LocalDate.now().plusDays(delivery.leadTimeDays.toLong()),
                ),
                processingTimeMillis = 0,
                threadName = threadName,
            )
        }

        // summary는 measureTimedValue에 의해 BlockingOrderSummaryResponse 해당 객체타입으로 정의 됨.
        // 해당 객체 타입에서 processingTimeMillis는 재할당 불가 컬럼 이므로 카피하여 processingTimeMillis = duration.inWholeMilliseconds로 값을 지정
        // 얕은 복사 이므로 Young Generation의 GC 대상, Minor GC에서 정리되어 리소스가 매우 저렴
        return summary.copy(processingTimeMillis = duration.inWholeMilliseconds)
    }
}
