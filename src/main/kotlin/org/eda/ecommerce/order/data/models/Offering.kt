package org.eda.ecommerce.order.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import java.util.*

@Entity
class Offering : PanacheEntityBase() {

    @Id
    lateinit var id: UUID

    var quantity: Int = 0
    lateinit var productId: UUID

    override fun toString(): String {
        return "Offering(id=$id, quantity=$quantity, productId=$productId)"
    }
}

