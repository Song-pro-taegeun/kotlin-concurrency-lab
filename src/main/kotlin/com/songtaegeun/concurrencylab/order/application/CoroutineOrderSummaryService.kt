package com.songtaegeun.concurrencylab.order.application

import com.songtaegeun.concurrencylab.delivery.application.BlockingDeliveryReader
import com.songtaegeun.concurrencylab.inventory.application.BlockingInventoryReader
import com.songtaegeun.concurrencylab.order.application.model.OrderSummaryReadResult
import com.songtaegeun.concurrencylab.order.dto.CoroutineOrderSummaryResponse
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryDeliveryResponse
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryInventoryResponse
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryProductResponse
import com.songtaegeun.concurrencylab.order.dto.OrderSummaryThreadNamesResponse
import com.songtaegeun.concurrencylab.product.application.BlockingProductReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.time.TimeSource

@Service
class CoroutineOrderSummaryService(
    private val productReader: BlockingProductReader,
    private val inventoryReader: BlockingInventoryReader,
    private val deliveryReader: BlockingDeliveryReader,
) {
    suspend fun get(productId: Long): CoroutineOrderSummaryResponse = coroutineScope {
        val startedAt = TimeSource.Monotonic.markNow()

        // JPA/JDBC와 Thread.sleep은 Blocking 작업이므로 Dispatchers.IO에서 실행한다.
        // async는 생성 즉시 실행되므로 세 조회는 동시에 시작된다.
        val productDeferred = async(Dispatchers.IO) {
            OrderSummaryReadResult(
                value = productReader.read(productId),
                threadName = Thread.currentThread().name,
            )
        }
        val inventoryDeferred = async(Dispatchers.IO) {
            OrderSummaryReadResult(
                value = inventoryReader.read(productId),
                threadName = Thread.currentThread().name,
            )
        }
        val deliveryDeferred = async(Dispatchers.IO) {
            OrderSummaryReadResult(
                value = deliveryReader.read(productId),
                threadName = Thread.currentThread().name,
            )
        }

        val productResult = productDeferred.await()
        val inventoryResult = inventoryDeferred.await()
        val deliveryResult = deliveryDeferred.await()

        CoroutineOrderSummaryResponse(
            product = OrderSummaryProductResponse(
                id = requireNotNull(productResult.value.id),
                name = productResult.value.name,
                price = productResult.value.price,
            ),
            inventory = OrderSummaryInventoryResponse(
                quantity = inventoryResult.value.quantity,
            ),
            delivery = OrderSummaryDeliveryResponse(
                leadTimeDays = deliveryResult.value.leadTimeDays,
                estimatedDeliveryDate = LocalDate.now()
                    .plusDays(deliveryResult.value.leadTimeDays.toLong()),
            ),
            processingTimeMillis = startedAt.elapsedNow().inWholeMilliseconds,
            threadNames = OrderSummaryThreadNamesResponse(
                product = productResult.threadName,
                inventory = inventoryResult.threadName,
                delivery = deliveryResult.threadName,
            ),
        )
    }
}
