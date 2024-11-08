package org.eda.ecommerce.helpers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eda.ecommerce.order.data.models.Offering
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.models.Order.OrderStatus
import org.eda.ecommerce.order.data.models.StockEntry
import org.eda.ecommerce.order.data.repositories.OfferingRepository
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.eda.ecommerce.order.data.repositories.StockRepository
import java.util.*

@ApplicationScoped
class EntityHelper {

    @Inject
    private lateinit var stockRepository: StockRepository

    @Inject
    private lateinit var offeringRepository: OfferingRepository

    @Inject
    private lateinit var orderRepository: OrderRepository

    @Transactional
    fun clearAllRepositories() {
        stockRepository.deleteAll()
        offeringRepository.deleteAll()
        orderRepository.deleteAll()
    }

    @Transactional
    fun createStockEntry(pProductId: UUID? = null, pAvailableStock: Int): StockEntry {
        val stockEntry = StockEntry().apply {
            productId = pProductId ?: UUID.randomUUID()
            availableStock = pAvailableStock
        }

        stockRepository.persist(stockEntry)

        println("[TEST] Created stock entry: $stockEntry")

        return stockEntry
    }

    @Transactional
    fun createOffering(
        pOfferingId: UUID? = null,
        pProductId: UUID? = null,
        pQuantity: Int = 1,
        pStatus: Offering.OfferingStatus = Offering.OfferingStatus.ACTIVE
    ): Offering {
        val offering = Offering().apply {
            id = pOfferingId ?: UUID.randomUUID()
            productId = pProductId ?: UUID.randomUUID()
            quantity = pQuantity
            status = pStatus
        }

        offeringRepository.persist(offering)

        println("[TEST] Created offering: $offering")

        return offering
    }

    @Transactional
    fun createOrder(): Order {
        val order = Order().apply {
            shoppingBasketId = UUID.randomUUID()
            customerId = UUID.randomUUID()
            orderDate = "2021-01-01"
            orderStatus = OrderStatus.Requested
            items = mutableListOf()
            products = mutableListOf()
        }

        orderRepository.persist(order)

        println("[TEST] Created order: $order")

        return order
    }
}
