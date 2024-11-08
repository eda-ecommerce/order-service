package org.eda.ecommerce.order.data.models.dto

import java.util.*
import kotlin.properties.Delegates

class StockDTO {

    lateinit var productId: UUID

    var availableStock by Delegates.notNull<Int>()
}

