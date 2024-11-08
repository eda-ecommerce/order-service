package org.eda.ecommerce.order.data.events.external.incoming.operations

import com.fasterxml.jackson.annotation.JsonValue

enum class ShippingEventOperation(@JsonValue override val value: String) : EventOperation {
    DELIVERED("ShipmentDelivered");

    companion object {
        fun from(search: String): ShippingEventOperation =
            requireNotNull(entries.find { it.value == search }) { "No ShippingEventOperation implemented for value $search" }
    }
}
