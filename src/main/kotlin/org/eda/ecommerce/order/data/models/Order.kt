package org.eda.ecommerce.order.data.models

import com.fasterxml.jackson.annotation.JsonValue
import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.util.*

@Entity(name = "Orders")
class Order : PanacheEntityBase() {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    lateinit var id: UUID

    lateinit var shoppingBasketId: UUID
    lateinit var customerId: UUID
    lateinit var orderDate: String
    lateinit var orderStatus: OrderStatus
    var totalPrice: Float? = null
    var totalItemQuantity: Int? = null

    @ElementCollection(fetch = FetchType.EAGER)
    lateinit var items: MutableList<ShoppingBasketItem>

    override fun toString(): String {
        return "ShoppingBasket(id=${id}, shoppingBasketId=$shoppingBasketId, customerId=$customerId, orderDate=$orderDate, orderStatus=$orderStatus, totalPrice=$totalPrice, totalItemQuantity=$totalItemQuantity items=$items)"
    }

    enum class OrderStatus(@JsonValue val value: String) {
        InProcess("InProcess"),
        Cancelled("Cancelled"),
        Completed("Completed"),
        Paid("Paid");
    }
}