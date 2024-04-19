package org.eda.ecommerce.services

import io.smallrye.reactive.messaging.MutinyEmitter
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eda.ecommerce.data.models.Order
import org.eda.ecommerce.data.models.events.OrderCreatedEvent
import org.eda.ecommerce.data.models.events.OrderUpdatedEvent
import org.eda.ecommerce.data.repositories.OrderRepository
import java.util.*

@ApplicationScoped
class OrderService {

    @Inject
    private lateinit var OrderRepository: OrderRepository

    @Inject
    @Channel("test-entity-out")
    private lateinit var OrderEmitter: MutinyEmitter<Order>

    fun getAll(): List<Order> {
        return OrderRepository.listAll()
    }

    fun findById(id: UUID): Order {
        return OrderRepository.findById(id)
    }

    fun deleteById(id: UUID): Boolean {
        val orderToDelete = OrderRepository.findById(id) ?: return false

        OrderRepository.delete(orderToDelete)

        OrderEmitter.sendMessageAndAwait(OrderUpdatedEvent(orderToDelete))

        return true
    }

    fun createNewEntity(Order: Order) {
        OrderRepository.persist(Order)

        OrderEmitter.sendMessageAndAwait(OrderCreatedEvent(Order))
    }

    fun updateOrder(order: Order): Boolean {
        val entity = OrderRepository.findById(order.id) ?: return false

        entity.apply {
            customerId = order.customerId
            orderDate = order.orderDate
            orderStatus = order.orderStatus
            totalPrice = order.totalPrice
            items = order.items
        }

        OrderRepository.persist(entity)


        OrderEmitter.sendMessageAndAwait(OrderUpdatedEvent(entity))

        return true
    }

}
