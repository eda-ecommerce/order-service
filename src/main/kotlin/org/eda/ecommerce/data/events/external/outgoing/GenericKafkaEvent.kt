package org.eda.ecommerce.data.events.external.outgoing

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata
import org.apache.kafka.common.header.internals.RecordHeaders
import org.eclipse.microprofile.reactive.messaging.Message
import org.eclipse.microprofile.reactive.messaging.Metadata

open class GenericKafkaEvent<T>(operation: String, value: T) : Message<T> {
    private val message: Message<T> = createMessageWithMetadata(value, operation)

    override fun getPayload(): T = message.payload
    override fun getMetadata(): Metadata = message.metadata

    companion object {
        private fun <T> createMessageWithMetadata(value: T, operation: String): Message<T> {
            val metadata = Metadata.of(
                OutgoingKafkaRecordMetadata.builder<String>()
                    .withHeaders(RecordHeaders().apply {
                        add("operation", operation.toByteArray())
                        add("source", "order".toByteArray())
                        add("timestamp", System.currentTimeMillis().toString().toByteArray())
                    }).build()
            )
            return Message.of(value, metadata)
        }
    }
}

open class GenericKafkaCreatedEvent<T>(value: T) : GenericKafkaEvent<T>("created", value)

open class GenericKafkaUpdatedEvent<T>(value: T) : GenericKafkaEvent<T>("updated", value)

open class GenericKafkaDeletedEvent<T>(value: T) : GenericKafkaEvent<T>("deleted", value)
