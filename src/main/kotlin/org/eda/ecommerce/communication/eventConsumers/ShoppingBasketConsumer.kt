package org.eda.ecommerce.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.data.models.ShoppingBasket

@ApplicationScoped
class ShoppingBasketConsumer {
    @Incoming("shopping-basket-in")
    @Transactional
    fun consume(record: ConsumerRecord<String, ShoppingBasket>) {
        val operation = record.headers().lastHeader("operation")
        println("Shopping Basket event operation: ${String(operation.value())}")

        println(record.value())
    }

}
