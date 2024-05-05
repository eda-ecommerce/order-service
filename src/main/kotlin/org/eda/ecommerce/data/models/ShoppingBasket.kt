package org.eda.ecommerce.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
class ShoppingBasket : PanacheEntityBase() {

    lateinit var shoppingBasketId: UUID

    lateinit var customerId: UUID
    lateinit var totalPrice: Number
    lateinit var totalItemQuantity: Number

    @ElementCollection
    lateinit var items: MutableList<ShoppingBasketItem>

    override fun toString(): String {
        return "ShoppingBasket(id=$shoppingBasketId, customerId=$customerId, totalPrice=$totalPrice, totalItemQuantity=$totalItemQuantity, items=$items)"
    }
}
