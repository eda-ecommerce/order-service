package org.eda.ecommerce.services

import io.quarkus.vertx.ConsumeEvent
import io.smallrye.common.annotation.Blocking
import io.smallrye.reactive.messaging.MutinyEmitter
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eda.ecommerce.data.events.external.incoming.StorableKafkaEvent
import org.eda.ecommerce.data.events.external.outgoing.OrderCreatedKafkaMessage
import org.eda.ecommerce.data.events.external.outgoing.OrderDeletedKafkaMessage
import org.eda.ecommerce.data.events.external.outgoing.OrderUpdatedKafkaMessage
import org.eda.ecommerce.data.models.Order
import org.eda.ecommerce.data.models.OrderStatus
import org.eda.ecommerce.data.models.ShoppingBasket
import org.eda.ecommerce.data.repositories.OrderRepository
import java.util.*

@ApplicationScoped
class OrderService {

    @Inject
    private lateinit var orderRepository: OrderRepository

    @Inject
    @Channel("order-out")
    private lateinit var orderEmitter: MutinyEmitter<Order>

    fun getAll(): List<Order> {
        return orderRepository.listAll()
    }

    fun findById(id: UUID): Order {
        return orderRepository.findById(id)
    }

    @Transactional
    fun deleteById(id: UUID): Boolean {
        val orderToDelete = orderRepository.findById(id) ?: return false

        orderRepository.delete(orderToDelete)

        orderEmitter.sendMessageAndAwait(OrderDeletedKafkaMessage(orderToDelete))

        return true
    }

    @ConsumeEvent("shopping-basket-checkout")
    @Blocking // TODO: this seems to be not a good idea... We might need to force every handler to be blocking OR figure out this magic event ending thing in the EventHandler
    fun createOrderFromShoppingBasket(orderCreatedEvent: StorableKafkaEvent<ShoppingBasket>) {
        println("Creating order from shopping basket: $orderCreatedEvent")

        val order = Order().apply {
            customerId = orderCreatedEvent.payload!!.customerId
            orderDate = orderCreatedEvent.timestamp
            orderStatus = OrderStatus.InProcess
            totalPrice = orderCreatedEvent.payload!!.totalPrice
            totalItemQuantity = orderCreatedEvent.payload!!.totalItemQuantity
            shoppingBasketId = orderCreatedEvent.payload!!.shoppingBasketId
            items = orderCreatedEvent.payload!!.items
        }

        persistAndSendEvent(order)
    }


    @Transactional
    fun persistAndSendEvent(order: Order) {
        orderRepository.persist(order)

        orderEmitter.sendMessageAndAwait(OrderCreatedKafkaMessage(order))
    }

    @Transactional
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


        orderEmitter.sendMessageAndAwait(OrderUpdatedKafkaMessage(entity))

        return true
    }

}
