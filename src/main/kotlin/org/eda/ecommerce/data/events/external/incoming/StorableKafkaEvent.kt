package org.eda.ecommerce.data.events.external.incoming

import com.fasterxml.jackson.annotation.JsonValue
import io.quarkus.hibernate.orm.panache.PanacheEntity

abstract class StorableKafkaEvent<T> : PanacheEntity(){
    lateinit var source: EventSource
    lateinit var operation: String
    lateinit var timestamp: String
    var payload: T? = null
    var processed: Boolean = false

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
            fun from(search: String): EventSource = requireNotNull(entries.find { it.value == search }) { "No EventSource with value $search" }
        }
    }
}


