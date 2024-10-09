package org.eda.ecommerce.order.data.models

import com.fasterxml.jackson.annotation.JsonValue
import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity
class Offering : PanacheEntityBase() {

    @Id
    lateinit var id: UUID

    var quantity: Int = 0
    lateinit var productId: UUID

    lateinit var status: OfferingStatus

    fun isAvailableToOrder(): Boolean {
        return status == OfferingStatus.ACTIVE
    }

    override fun toString(): String {
        return "Offering(id=$id, quantity=$quantity, productId=$productId), status=$status"
    }

    enum class OfferingStatus(@JsonValue val value: String) {
        ACTIVE("active"),
        INACTIVE("inactive"),
        RETIRED("retired");
    }

}

