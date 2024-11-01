package org.eda.ecommerce.order.data.models.dto

import java.util.*
import kotlin.properties.Delegates

class StockDTO {

    lateinit var productId: UUID

    var actualStock by Delegates.notNull<Int>()

    var reservedStock by Delegates.notNull<Int>()

    var availableStock by Delegates.notNull<Int>()
}

