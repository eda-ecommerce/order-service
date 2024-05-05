package org.eda.ecommerce.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.data.models.ShoppingBasket
import org.eda.ecommerce.data.repositories.ShoppingBasketKafkaEventRepository
import org.eda.ecommerce.services.EventHandlerService

@ApplicationScoped
class ShoppingBasketConsumer {

    @Inject
    private lateinit var shoppingBasketKafkaEventRepository: ShoppingBasketKafkaEventRepository

    @Inject
    private lateinit var eventHandlerService: EventHandlerService

    @Incoming("shopping-basket-in")
    fun consume(record: ConsumerRecord<String, ShoppingBasket>) {
        val operation = record.headers().lastHeader("operation")
        println("Received Shopping Basket event with operation: ${String(operation.value())}")
        println(record.value())

        eventHandlerService.storeAndProcessEvent(record, shoppingBasketKafkaEventRepository)
    }
}
