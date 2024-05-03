package org.eda.ecommerce.data.models

import com.fasterxml.jackson.annotation.JsonValue
import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import io.quarkus.hibernate.orm.panache.PanacheEntity_.id
import jakarta.persistence.*
import java.util.*

@Entity
class Order : PanacheEntityBase() {
    @Id
    lateinit var id: UUID

    lateinit var shoppingBasketId: UUID
    lateinit var customerId: UUID
    lateinit var orderDate: String
    lateinit var orderStatus: OrderStatus
    lateinit var totalPrice: Number

    @ElementCollection
    lateinit var items: MutableList<ShoppingBasketItem>

    override fun toString(): String {
        return "ShoppingBasket(id=${id}, shoppingBasketId=$shoppingBasketId, customerId=$customerId, orderDate=$orderDate, orderStatus=$orderStatus, totalPrice=$totalPrice, items=$items)"
    }
}

enum class OrderStatus(@JsonValue val value: String) {
    InProcess("InProcess"),
    Cancelled("Cancelled"),
    Completed("Completed"),
    Paid("Paid");
}

class OrderDTO {
    lateinit var customerId: UUID
    lateinit var shoppingBasketId: UUID
    lateinit var orderDate: String
    lateinit var orderStatus: OrderStatus
    lateinit var totalPrice: Number
    lateinit var items: MutableList<ShoppingBasketItem>

    fun toOrder(): Order {
        val order = Order()
        order.shoppingBasketId = shoppingBasketId
        order.customerId = customerId
        order.orderDate = orderDate
        order.orderStatus = orderStatus
        order.totalPrice = totalPrice
        order.items = items
        return order
    }

    override fun toString(): String {
        return "ShoppingBasket(id=${id}, shoppingBasketId=$shoppingBasketId, customerId=$customerId, orderDate=$orderDate, orderStatus=$orderStatus, totalPrice=$totalPrice, items=$items)"
    }
}
