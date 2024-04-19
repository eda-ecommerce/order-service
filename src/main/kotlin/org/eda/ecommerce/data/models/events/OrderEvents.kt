package org.eda.ecommerce.data.models.events

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata
import org.apache.kafka.common.header.internals.RecordHeaders
import org.eclipse.microprofile.reactive.messaging.Message
import org.eclipse.microprofile.reactive.messaging.Metadata
import org.eda.ecommerce.data.models.Order

open class OrderEvent(operation: String, Order: Order) : Message<Order> {
    private val message: Message<Order> = createMessageWithMetadata(Order, operation)

    override fun getPayload(): Order = message.payload
    override fun getMetadata(): Metadata = message.metadata
    companion object {
        private fun createMessageWithMetadata(Order: Order, operation: String): Message<Order> {
            val metadata = Metadata.of(
                OutgoingKafkaRecordMetadata.builder<String>()
                    .withHeaders(RecordHeaders().apply {
                        add("operation", operation.toByteArray())
                        add("source", "Order".toByteArray())
                        add("timestamp", System.currentTimeMillis().toString().toByteArray())
                    }).build()
            )
            return Message.of(Order, metadata)
        }
    }
}

class OrderCreatedEvent(order: Order) : OrderEvent("created", order)

class OrderUpdatedEvent(order: Order) : OrderEvent("updated", order)

class OrderDeletedEvent(order: Order) : OrderEvent("deleted", order)
