package org.eda.ecommerce.order.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.order.data.events.external.incoming.ShoppingBasketEvent
import org.eda.ecommerce.order.data.models.ShoppingBasket
import org.eda.ecommerce.order.services.OrderService
import org.eda.ecommerce.order.data.events.external.incoming.EDAEvent.EventSource
import org.eda.ecommerce.order.data.events.external.incoming.EDAEvent.EventOperation

@ApplicationScoped
class ShoppingBasketConsumer {

    @Inject
    private lateinit var orderService: OrderService

    @Incoming("shopping-basket-in")
    fun consume(record: ConsumerRecord<String, ShoppingBasket>) {
        val operation = record.headers().lastHeader("operation")
        println("Received Shopping Basket event with operation: ${String(operation.value())}")
        println(record.value())

        val event = ShoppingBasketEvent(
            source = EventSource.from(String(record.headers().lastHeader("source").value())),
            operation = EventOperation.from(String(record.headers().lastHeader("operation").value())),
            timestamp = record.headers().lastHeader("timestamp").toString(),
            payload = record.value()
        )

        orderService.createOrderFromShoppingBasket(event)
    }
}
