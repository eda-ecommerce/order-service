package org.eda.ecommerce.order.services

import io.smallrye.reactive.messaging.MutinyEmitter
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eda.ecommerce.order.data.events.external.outgoing.OrderFulfilledKafkaMessage
import org.eda.ecommerce.order.data.events.external.outgoing.OrderRequestedKafkaMessage
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.models.Order.OrderStatus
import org.eda.ecommerce.order.data.models.ProductQuantity
import org.eda.ecommerce.order.data.models.ShoppingBasket
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.eda.ecommerce.order.exceptions.EventProcessingException
import org.eda.ecommerce.order.exceptions.InvalidShoppingBasketException
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
    fun createOrderFromShoppingBasket(shoppingBasket: ShoppingBasket) {
        println("Creating order from shopping basket: $shoppingBasket")

        // Map Offerings to Products (aka sum up individual product counts) and store those alongside the offerings in the Order
        val shoppingBasketItems = shoppingBasket.items
        val productQuantities = mutableListOf<ProductQuantity>()
        shoppingBasketItems.forEach { item ->
            val offering = try {
                offeringService.getOfferingIfAvailableForOrder(item.offeringId)
            } catch (e: EventProcessingException) {
                throw InvalidShoppingBasketException(shoppingBasket.shoppingBasketId, e.message ?: "")
            }

            val totalQuantity = offering.quantity * item.quantity
            productQuantities.add(ProductQuantity(offering.productId, totalQuantity))
        }

        val order = Order().apply {
            customerId = shoppingBasket.customerId
            orderDate = Date().toString()
            orderStatus = OrderStatus.Requested
            totalPrice = shoppingBasket.totalPrice
            totalItemQuantity = shoppingBasket.totalItemQuantity
            shoppingBasketId = shoppingBasket.shoppingBasketId
            items = shoppingBasketItems
            products = productQuantities
        }

        orderRepository.persist(order)

        println("Created order: $order")

        orderEmitter.sendMessageAndAwait(OrderRequestedKafkaMessage(order))
    }

    @Transactional
    fun handleDeliveryById(orderId: UUID) {
        val order = orderRepository.findById(orderId)
        order.orderStatus = OrderStatus.Fulfilled
        orderRepository.persist(order)

        println("Order $orderId has been marked as Fulfilled")

        orderEmitter.sendMessageAndAwait(OrderFulfilledKafkaMessage(order))
    }

}
