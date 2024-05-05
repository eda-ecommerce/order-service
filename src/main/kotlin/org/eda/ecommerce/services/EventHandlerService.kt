package org.eda.ecommerce.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eda.ecommerce.data.events.external.incoming.StorableKafkaEvent
import org.eda.ecommerce.data.repositories.GenericKafkaEventRepository

/**
 * This service will take in the external events, store them and process/split them into internal event bus events.
 * After dispatching the internal events, it will wait for all of them to complete until marking the external event as processed.
 *
 * It also contains startup logic that looks at any unprocessed events and processes them in the same manner.
 */
@ApplicationScoped
class EventHandlerService {

    fun <T, ET : StorableKafkaEvent<T>> storeAndProcessEvent(rawRecord: ConsumerRecord<String, T>, topicRepository: GenericKafkaEventRepository<T, ET>) {
        val event = storeEvent(rawRecord, topicRepository)

        println("Stored event: $event")

        processEvent(event)

        println("Event processed: $event")

        persistEventWorkSuccess(event, topicRepository)

        println("Event persisted: $event")
    }

    /**
     * This creates an event of the given type ET and saves it to the database.
     * ET is the same type the repository stores and something that extends StorableKafkaEvent.
     *
     * This is done in its own transaction to ensure that the event is stored before processing.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun <T, ET : StorableKafkaEvent<T>> storeEvent(rawRecord: ConsumerRecord<String, T>, topicRepository: GenericKafkaEventRepository<T, ET>): ET {
        println("Storing event with transaction")
        return topicRepository.createAndStoreEvent(
            operation = String(rawRecord.headers().lastHeader("operation").value()),
            source = String(rawRecord.headers().lastHeader("source").value()),
            timestamp = String(rawRecord.headers().lastHeader("timestamp").value()),
            payload = rawRecord.value()
        )
    }


    fun <T> processEvent(event: StorableKafkaEvent<T>) {
        println("Processing event: $event")

        // TODO: Dispatch the event to the internal event bus
        // TODO: Wait for all internal events to complete
        // TODO: Mark the external event as processed

    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun <T, ET : StorableKafkaEvent<T>> persistEventWorkSuccess(event: StorableKafkaEvent<T>, topicRepository: GenericKafkaEventRepository<T, ET>) {
        event.finalize()
        topicRepository.persist(event)
    }

    fun processUnprocessedEvents() {
        // TODO: Function to run at startup to process any unprocessed events
    }
}
