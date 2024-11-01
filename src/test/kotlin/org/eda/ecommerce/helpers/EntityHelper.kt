package org.eda.ecommerce.helpers

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
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
}
