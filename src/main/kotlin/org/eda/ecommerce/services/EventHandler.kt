package org.eda.ecommerce.services

import io.vertx.mutiny.core.eventbus.EventBus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eda.ecommerce.data.events.external.incoming.StorableKafkaEvent
import org.eda.ecommerce.data.repositories.GenericKafkaEventRepository

/**
 * This service will take in the external events, store them and process/split them into internal events.
 * After dispatching the internal events into the event bus, it will wait for all of them to complete until marking the external event as processed.
 *
 * It also contains startup logic that looks at any unprocessed events and processes them in the same manner.
 */
@ApplicationScoped
class EventHandler {

    @Inject
    private lateinit var eventBus: EventBus

    fun <T, ET : StorableKafkaEvent<T>> storeAndProcessEvent(
        rawRecord: ConsumerRecord<String, T>,
        topicRepository: GenericKafkaEventRepository<T, ET>
    ) {
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
    fun <T, ET : StorableKafkaEvent<T>> storeEvent(
        rawRecord: ConsumerRecord<String, T>,
        topicRepository: GenericKafkaEventRepository<T, ET>
    ): ET {
        println("Storing event with transaction")
        return topicRepository.createAndStoreEvent(
            operation = String(rawRecord.headers().lastHeader("operation").value()),
            source = String(rawRecord.headers().lastHeader("source").value()),
            timestamp = String(rawRecord.headers().lastHeader("timestamp").value()),
            payload = rawRecord.value()
        )
    }


    /**
     * This processes the event by dispatching it to the internal event bus.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun <T, ET : StorableKafkaEvent<T>> processEvent(event: ET) {
        println("Processing event: $event")

        // TODO: Parse/breakdown the external events into smaller, more meaningful internal events
        // TODO: Figure out if this is the right place for it.
        when (event.source) {
            StorableKafkaEvent.EventSource.SHOPPING_BASKET -> {
                eventBus.publish("shopping-basket-checkout", event)
            }

            StorableKafkaEvent.EventSource.PAYMENT -> {
                eventBus.publish("payment-updated", event)
            }
        }

        // TODO: Check if this is actually finished at this point...
        //       It probably isn't and we need to do some magic to find out if everything finished.
        //       I did not figure anything out for a few hours, so it might be futile...
        //       But if we can force each listener to have a transaction on its own, this might not be a problem.
        println("Internal event dispatched: $event")
    }

    /**
     * Sets the flag on the event to processed and persists it.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun <T, ET : StorableKafkaEvent<T>> persistEventWorkSuccess(
        event: ET,
        topicRepository: GenericKafkaEventRepository<T, ET>
    ) {
        event.finalize()
        topicRepository.merge(event)
    }

    fun processUnprocessedEvents() {
        // TODO: Function to run at startup to process any unprocessed events
    }
}
