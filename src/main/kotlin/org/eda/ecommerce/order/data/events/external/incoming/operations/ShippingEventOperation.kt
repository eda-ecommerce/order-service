package org.eda.ecommerce.order.data.events.external.incoming.operations

import com.fasterxml.jackson.annotation.JsonValue

enum class ShippingEventOperation(@JsonValue override val value: String) : EventOperation {
    DELIVERED("shipmentdelivered"),
    UNKNOWN("unknown");

    companion object {
        fun from(search: String): ShippingEventOperation =
            entries.find { it.value == search } ?: UNKNOWN
    }
}
