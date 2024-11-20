package org.eda.ecommerce.order.data.models.dto

import org.eda.ecommerce.order.data.models.Offering
import org.eda.ecommerce.order.data.models.Offering.OfferingStatus
import java.util.*

class OfferingDTO {

    lateinit var id: UUID
    var quantity: Int = 0

    lateinit var status: OfferingStatus

    lateinit var product: Product

    class Product {
        lateinit var id: UUID
    }

    fun toEntity() : Offering {
        val offering = Offering()
        offering.id = id
        offering.quantity = quantity
        offering.productId = product.id
        offering.status = status
        return offering
    }
}
