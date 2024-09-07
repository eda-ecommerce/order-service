package org.eda.ecommerce.order.data.models

import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
class ProductQuantity() {
    constructor(productId: UUID, quantity: Int?) : this() {
        this.productId = productId
        this.quantity = quantity
    }

    lateinit var productId: UUID
    var quantity: Int? = null

    override fun toString(): String {
        return "ProductQuantity(productId=$productId, quantity=$quantity)"
    }

}
