package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import org.eda.ecommerce.data.events.external.incoming.StorableKafkaEvent
import java.util.*
import kotlin.reflect.KClass

open class GenericKafkaEventRepository<T, ET : StorableKafkaEvent<T>>(private val eventClass: KClass<ET>) :
    PanacheRepositoryBase<StorableKafkaEvent<T>, UUID> {

    fun createAndStoreEvent(operation: String, source: String, timestamp: String, payload: T): ET {
        val event = eventClass.java.getDeclaredConstructor().newInstance()
        event.operation = StorableKafkaEvent.EventOperation.from(operation)
        event.source = StorableKafkaEvent.EventSource.from(source)
        event.timestamp = timestamp
        event.payload = payload
        this.persist(event)
        return event
    }

    fun merge(entity: ET) {
        entityManager.merge(entity)
    }

    fun findUnprocessedEvents(): List<StorableKafkaEvent<T>> {
        return list("processed", false)
    }
}
