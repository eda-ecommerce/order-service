package org.eda.ecommerce.order.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.order.data.events.external.incoming.operations.ShoppingBasketEventOperation
import org.eda.ecommerce.order.data.models.ShoppingBasket
import org.eda.ecommerce.order.exceptions.EmptyEventPayloadException
import org.eda.ecommerce.order.services.OrderService

@ApplicationScoped
class ShoppingBasketConsumer {

    @Inject
    private lateinit var orderService: OrderService

    @Incoming("shopping-basket-in")
    fun consume(record: ConsumerRecord<String, ShoppingBasket>) {
        val operation = String(record.headers().lastHeader("operation").value())
        println("Received Shopping Basket event with operation: $operation")

        val shoppingBasket = record.value() ?: throw EmptyEventPayloadException(
            "Shopping Basket",
            operation
        )

        when (ShoppingBasketEventOperation.from(operation)) {
            ShoppingBasketEventOperation.CHECKOUT -> orderService.createOrderFromShoppingBasket(shoppingBasket)
        }
    }
}
