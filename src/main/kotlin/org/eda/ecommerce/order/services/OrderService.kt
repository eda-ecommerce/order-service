package org.eda.ecommerce.order.services

import io.smallrye.reactive.messaging.MutinyEmitter
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eda.ecommerce.order.data.events.external.incoming.EDAEvent
import org.eda.ecommerce.order.data.events.external.outgoing.OrderCreatedKafkaMessage
import org.eda.ecommerce.order.data.events.external.outgoing.OrderDeletedKafkaMessage
import org.eda.ecommerce.order.data.events.external.outgoing.OrderUpdatedKafkaMessage
import org.eda.ecommerce.order.data.models.Offering
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.models.Order.OrderStatus
import org.eda.ecommerce.order.data.models.ProductQuantity
import org.eda.ecommerce.order.data.models.ShoppingBasket
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.eda.ecommerce.order.exceptions.OfferingNotActiveException
import org.eda.ecommerce.order.exceptions.OfferingNotFoundException
import java.util.*

@ApplicationScoped
class OrderService {

    @Inject
    private lateinit var orderRepository: OrderRepository

    @Inject
    private lateinit var offeringService: OfferingService

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

    @Transactional
    fun createOrderFromShoppingBasket(orderCreatedEvent: EDAEvent<ShoppingBasket>) {
        val shoppingBasket = orderCreatedEvent.payload!!

        println("Creating order from shopping basket: $shoppingBasket")

        // Map Offerings to Products (aka sum up individual product counts) and store those alongside the offerings in the Order
        val shoppingBasketItems = shoppingBasket.items
        val productQuantities = mutableListOf<ProductQuantity>()
        shoppingBasketItems.forEach { item ->
            val offering = offeringService.findById(item.offeringId) ?: throw OfferingNotFoundException(item.offeringId, "Cannot create order from shopping basket ${shoppingBasket.shoppingBasketId}")

            if (offering.status != Offering.OfferingStatus.ACTIVE) {
                throw OfferingNotActiveException(item.offeringId, "Cannot create order from shopping basket ${shoppingBasket.shoppingBasketId}")
            }

            val totalQuantity = offering.quantity.times(item.quantity)

            productQuantities.add(ProductQuantity(offering.productId, totalQuantity))
        }

        val order = Order().apply {
            customerId = shoppingBasket.customerId
            orderDate = Date().toString()
            orderStatus = OrderStatus.InProcess
            totalPrice = shoppingBasket.totalPrice
            totalItemQuantity = shoppingBasket.totalItemQuantity
            shoppingBasketId = shoppingBasket.shoppingBasketId
            items = shoppingBasketItems
            products = productQuantities
        }

        orderRepository.persist(order)

        println("Created order: $order")

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
