package org.eda.ecommerce.order.data.events.external.incoming.operations

import com.fasterxml.jackson.annotation.JsonValue
import org.eda.ecommerce.order.data.events.external.incoming.EDAEvent.EventOperation

enum class StockEventOperation(@JsonValue override val value: String) : EventOperation {
    ADJUSTED("AvailableStockAdjusted");

    companion object {
        fun from(search: String): StockEventOperation =
            requireNotNull(entries.find { it.value == search }) { "No StockEventOperation implemented for value $search" }
    }
}
