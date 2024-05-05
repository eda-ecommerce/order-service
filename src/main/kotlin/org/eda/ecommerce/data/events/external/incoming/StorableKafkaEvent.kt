package org.eda.ecommerce.data.events.external.incoming

import com.fasterxml.jackson.annotation.JsonValue
import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.GenericGenerator
import java.util.*

@MappedSuperclass
abstract class StorableKafkaEvent<T> : PanacheEntityBase() {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    open lateinit var id: UUID

    open lateinit var source: EventSource
    open lateinit var operation: EventOperation
    open lateinit var timestamp: String

    @Embedded
    open var payload: T? = null

    open var processed: Boolean = false

    fun finalize(status: Boolean = true) {
        this.processed = status
    }

    override fun toString(): String {
        return "StorableKafkaEvent(id=$id, source=$source, operation=$operation, timestamp=$timestamp, processed=$processed, payload=$payload)"
    }

    enum class EventSource(@JsonValue val value: String) {
        SHOPPING_BASKET("shopping-basket-service"),
        PAYMENT("payment");

        companion object {
            fun from(search: String): EventSource =
                requireNotNull(entries.find { it.value == search }) { "No EventSource with value $search" }
        }
    }

    enum class EventOperation(@JsonValue val value: String) {
        CREATED("CREATED"),
        UPDATED("UPDATED"),
        CHECKOUT("CHECKOUT");

        companion object {
            fun from(search: String): EventOperation =
                requireNotNull(entries.find { it.value == search }) { "No EventOperation with value $search" }
        }
    }
}


