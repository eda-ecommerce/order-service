package org.eda.ecommerce.order.data.events.external.incoming.operations

import com.fasterxml.jackson.annotation.JsonValue

enum class OfferingEventOperation(@JsonValue override val value: String) : EventOperation {
    CREATED("created"),
    UPDATED("updated");

    companion object {
        fun from(search: String): OfferingEventOperation =
            requireNotNull(entries.find { it.value == search }) { "No OfferingEventOperation implemented for value $search" }
    }
}
