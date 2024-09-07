package org.eda.ecommerce.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
class ShoppingBasket : PanacheEntityBase() {

    lateinit var shoppingBasketId: UUID

    lateinit var customerId: UUID
    var totalPrice: Float? = null
    var totalItemQuantity: Int? = null

    @ElementCollection
    lateinit var items: MutableList<ShoppingBasketItem>

    override fun toString(): String {
        return "ShoppingBasket(id=$shoppingBasketId, customerId=$customerId, totalPrice=$totalPrice, totalItemQuantity=$totalItemQuantity, items=$items)"
    }
}
