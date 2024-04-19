package org.eda.ecommerce.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import io.quarkus.hibernate.orm.panache.PanacheEntity_.id
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.util.*

@Entity
class ShoppingBasket : PanacheEntityBase() {
    @Id
    lateinit var shoppingBasketId: UUID

    lateinit var customerId: UUID
    lateinit var totalPrice: Number
    lateinit var totalItemQuantity: Number

    @OneToMany
    lateinit var items: MutableList<ShoppingBasketItem>

    override fun toString(): String {
        return "ShoppingBasket(id=$shoppingBasketId, customerId=$customerId, totalPrice=$totalPrice, totalItemQuantity=$totalItemQuantity, items=$items)"
    }
}

class ShoppingBasketDTO {
    lateinit var customerId: UUID
    lateinit var totalPrice: Number
    lateinit var totalItemQuantity: Number
    lateinit var items: MutableList<ShoppingBasketItem>

    fun toShoppingBasket(): ShoppingBasket {
        val shoppingBasket = ShoppingBasket()
        shoppingBasket.customerId = customerId
        shoppingBasket.totalPrice = totalPrice
        shoppingBasket.totalItemQuantity = totalItemQuantity
        shoppingBasket.items = items
        return shoppingBasket
    }

    override fun toString(): String {
        return "ShoppingBasket(id=${id}, customerId=$customerId, totalPrice=$totalPrice, totalItemQuantity=$totalItemQuantity, items=$items)"
    }
}
