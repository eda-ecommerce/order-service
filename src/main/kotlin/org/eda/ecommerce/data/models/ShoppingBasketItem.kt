package org.eda.ecommerce.data.models

import com.fasterxml.jackson.annotation.JsonValue
import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import io.quarkus.hibernate.orm.panache.PanacheEntity_.id
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.*

@Entity
class ShoppingBasketItem : PanacheEntityBase() {

    @Id
    lateinit var shoppingBasketItemId: UUID

    lateinit var shoppingBasketId: UUID

    lateinit var offeringId: UUID
    lateinit var quantity: Number
    lateinit var totalPrice: Number
    lateinit var itemState: ItemState

    override fun toString(): String {
        return "ShoppingBasketItem(id=$shoppingBasketItemId, shoppingBasketId=$shoppingBasketId, offeringId=$offeringId, quantity=$quantity, totalPrice=$totalPrice, itemState=$itemState)"
    }
}


enum class ItemState(@JsonValue val value: String) {
    AVAILABLE("AVAILABLE"),
    UNAVAILABLE("UNAVAILABLE");
}

class ShoppingBasketItemDTO {
    lateinit var shoppingBasketId: UUID
    lateinit var offeringId: UUID
    lateinit var quantity: Number
    lateinit var totalPrice: Number
    lateinit var itemState: ItemState

    fun toShoppingBasketItem(): ShoppingBasketItem {
        val shoppingBasketItem = ShoppingBasketItem()
        shoppingBasketItem.shoppingBasketId = shoppingBasketId
        shoppingBasketItem.offeringId = offeringId
        shoppingBasketItem.quantity = quantity
        shoppingBasketItem.totalPrice = totalPrice
        shoppingBasketItem.itemState = itemState
        return shoppingBasketItem
    }

    override fun toString(): String {
        return "ShoppingBasketItem(id=${id}, shoppingBasketId=$shoppingBasketId, offeringId=$offeringId, quantity=$quantity, totalPrice=$totalPrice, itemState=$itemState)"
    }
}
