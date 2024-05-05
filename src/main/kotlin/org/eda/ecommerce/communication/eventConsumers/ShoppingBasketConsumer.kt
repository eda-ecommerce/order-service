package org.eda.ecommerce.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.data.events.external.incoming.ShoppingBasketEvent
import org.eda.ecommerce.data.models.ShoppingBasket
import org.eda.ecommerce.data.repositories.ShoppingBasketKafkaEventRepository

@ApplicationScoped
class ShoppingBasketConsumer {

    @Inject
    private lateinit var shoppingBasketKafkaEventRepository: ShoppingBasketKafkaEventRepository

    @Incoming("shopping-basket-in")
    @Transactional
    fun consume(record: ConsumerRecord<String, ShoppingBasket>) {
        val operation = record.headers().lastHeader("operation")
        println("Shopping Basket event operation: ${String(operation.value())}")

        println(record.value())

        persistKafkaEvent(record)
    }


    fun persistKafkaEvent(record: ConsumerRecord<String, ShoppingBasket>) {
        val event = ShoppingBasketEvent(
            operation = String(record.headers().lastHeader("operation").value()),
            source = String(record.headers().lastHeader("source").value()),
            timestamp = String(record.headers().lastHeader("timestamp").value()),
            payload = record.value()
        )

        println("Persisting event: $event")

        shoppingBasketKafkaEventRepository.persist(event)
    }
}
