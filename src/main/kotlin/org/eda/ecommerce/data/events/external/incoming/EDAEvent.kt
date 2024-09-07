package org.eda.ecommerce.data.events.external.incoming

import com.fasterxml.jackson.annotation.JsonValue

abstract class EDAEvent<T>  {

    open lateinit var source: EventSource
    open lateinit var operation: EventOperation
    open lateinit var timestamp: String

    open var payload: T? = null

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


