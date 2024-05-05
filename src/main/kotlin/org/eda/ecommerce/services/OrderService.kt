package org.eda.ecommerce.services

import io.quarkus.vertx.ConsumeEvent
import io.smallrye.reactive.messaging.MutinyEmitter
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eda.ecommerce.data.events.external.incoming.StorableKafkaEvent
import org.eda.ecommerce.data.events.external.outgoing.OrderCreatedKafkaEvent
import org.eda.ecommerce.data.events.external.outgoing.OrderDeletedKafkaEvent
import org.eda.ecommerce.data.events.external.outgoing.OrderUpdatedKafkaEvent
import org.eda.ecommerce.data.models.Order
import org.eda.ecommerce.data.models.ShoppingBasket
import org.eda.ecommerce.data.repositories.OrderRepository
import java.util.*

@ApplicationScoped
class OrderService {

    @Inject
    private lateinit var orderRepository: OrderRepository

    @Inject
    @Channel("test-entity-out")
    private lateinit var orderEmitter: MutinyEmitter<Order>

    fun getAll(): List<Order> {
        return orderRepository.listAll()
    }

    fun findById(id: UUID): Order {
        return orderRepository.findById(id)
    }

    fun deleteById(id: UUID): Boolean {
        val orderToDelete = orderRepository.findById(id) ?: return false

        orderRepository.delete(orderToDelete)

        orderEmitter.sendMessageAndAwait(OrderDeletedKafkaEvent(orderToDelete))

        return true
    }

    @ConsumeEvent("shopping-basket-checkout")
    fun createOrderFromShoppingBasket(orderCreatedEvent: StorableKafkaEvent<ShoppingBasket>) {
        println("Creating order from shopping basket: $orderCreatedEvent")
        // TODO: Implement this
    }


    fun createNewEntity(order: Order) {
        orderRepository.persist(order)

        orderEmitter.sendMessageAndAwait(OrderCreatedKafkaEvent(order))
    }

    fun updateOrder(order: Order): Boolean {
        val entity = orderRepository.findById(order.id) ?: return false

        entity.apply {
            customerId = order.customerId
            orderDate = order.orderDate
            orderStatus = order.orderStatus
            totalPrice = order.totalPrice
            items = order.items
        }

        orderRepository.persist(entity)


        orderEmitter.sendMessageAndAwait(OrderUpdatedKafkaEvent(entity))

        return true
    }

}
