package org.eda.ecommerce.data.models

import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
class ShoppingBasketItem {

    lateinit var shoppingBasketItemId: UUID

    lateinit var shoppingBasketId: UUID

    lateinit var offeringId: UUID
    lateinit var quantity: Number
    lateinit var totalPrice: Number
    lateinit var itemState: ItemState

    override fun toString(): String {
        return "ShoppingBasketItem(id=$shoppingBasketItemId, shoppingBasketId=$shoppingBasketId, offeringId=$offeringId, quantity=$quantity, totalPrice=$totalPrice, itemState=$itemState)"
    }

    enum class ItemState(@JsonValue val value: String) {
        AVAILABLE("AVAILABLE"),
        UNAVAILABLE("UNAVAILABLE");
    }

}
