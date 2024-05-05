package org.eda.ecommerce.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity
class ShoppingBasket : PanacheEntityBase() {
    @Id
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
