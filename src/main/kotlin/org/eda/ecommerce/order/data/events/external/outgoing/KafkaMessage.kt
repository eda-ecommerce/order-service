package org.eda.ecommerce.order.data.events.external.outgoing

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata
import org.apache.kafka.common.header.internals.RecordHeaders
import org.eclipse.microprofile.reactive.messaging.Message
import org.eclipse.microprofile.reactive.messaging.Metadata

open class KafkaMessage<T>(operation: String, value: T) : Message<T> {
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

open class KafkaCreatedMessage<T>(value: T) : KafkaMessage<T>("created", value)

open class KafkaUpdatedMessage<T>(value: T) : KafkaMessage<T>("updated", value)

open class KafkaDeletedMessage<T>(value: T) : KafkaMessage<T>("deleted", value)
