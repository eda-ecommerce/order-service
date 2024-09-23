package org.eda.ecommerce.order.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.order.data.events.external.incoming.EDAEvent.EventOperation
import org.eda.ecommerce.order.data.events.external.incoming.EDAEvent.EventSource
import org.eda.ecommerce.order.data.events.external.incoming.ShoppingBasketEvent
import org.eda.ecommerce.order.data.models.ShoppingBasket
import org.eda.ecommerce.order.exceptions.EmptyEventPayloadException
import org.eda.ecommerce.order.services.OrderService

@ApplicationScoped
class ShoppingBasketConsumer {

    @Inject
    private lateinit var orderService: OrderService

    @Incoming("shopping-basket-in")
    fun consume(record: ConsumerRecord<String, ShoppingBasket>) {
        val operation = record.headers().lastHeader("operation")
        println("Received Shopping Basket event with operation: ${String(operation.value())}")

        // TODO: Decide if we even need the event representation since we know the payload type and don't care about the headers except for "operation".
        //       The nice enum for that is nice, but can be done without the overhead of converting to a full event representation.
        //       In the future we might care about other fields, but for now, the whole round trip via an EDAEvent instance like ShoppingBasketEvent in this case feels redundant...
        val event = ShoppingBasketEvent(
            source = EventSource.from(String(record.headers().lastHeader("source").value())),
            operation = EventOperation.from(String(record.headers().lastHeader("operation").value())),
            timestamp = record.headers().lastHeader("timestamp").toString(),
            payload = record.value()
        )

        val shoppingBasket = event.payload ?: throw EmptyEventPayloadException(
            "Shopping Basket",
            event.operation.toString()
        )

        when (event.operation) {
            EventOperation.CHECKOUT -> orderService.createOrderFromShoppingBasket(shoppingBasket)
            EventOperation.CREATED -> throw NotImplementedError("Shopping Basket creation is not supported")
            EventOperation.UPDATED -> throw NotImplementedError("Shopping Basket update is not supported")
        }
    }
}
