package org.eda.ecommerce.order.data.events.external.incoming.operations

import com.fasterxml.jackson.annotation.JsonValue

enum class ShoppingBasketEventOperation(@JsonValue override val value: String) : EventOperation {
    CHECKOUT("checkout");

    companion object {
        fun from(search: String): ShoppingBasketEventOperation =
            requireNotNull(entries.find { it.value == search }) { "No ShoppingBasketEventOperation with value $search" }
    }
}
