package org.eda.ecommerce.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import io.quarkus.hibernate.orm.panache.PanacheEntity_
import io.quarkus.hibernate.orm.panache.PanacheEntity_.id
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import sun.jvm.hotspot.oops.CellTypeState.value
import java.util.UUID

@Entity
class ShoppingBasket : PanacheEntityBase() {
    @Id
    lateinit var id: UUID

    lateinit var customerId: UUID
    lateinit var totalPrice: Number
    lateinit var totalItemQuantity: Number

    @OneToMany(mappedBy = "shoppingBasketId")
    lateinit var items: MutableList<ShoppingBasketItem>

    override fun toString(): String {
        return "ShoppingBasket(id=$id, customerId=$customerId, totalPrice=$totalPrice, totalItemQuantity=$totalItemQuantity, items=$items)"
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
