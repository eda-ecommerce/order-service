package org.eda.ecommerce.order.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import io.quarkus.hibernate.orm.panache.PanacheEntity_.id
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity
class StockEntry {

    @Id
    @Column(name = "id")
    lateinit var productId: UUID

    var availableStock: Int = 0

    override fun toString(): String {
        return "Stock(id=$id, productId=$productId, actualStock=$availableStock)"
    }

    fun isAvailableToOrder(requestedAmount: Int = 0): Boolean {
        return availableStock >= requestedAmount
    }

}

