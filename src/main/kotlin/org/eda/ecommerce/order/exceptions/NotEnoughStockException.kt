package org.eda.ecommerce.order.exceptions

import java.util.*

class NotEnoughStockException : EventProcessingException {
    constructor() : super("Not enough stock available.")
    constructor(productId: UUID) : super("Not enough stock for product with id $productId available.")

    constructor(productId: UUID, failureMode: String) : super("Not enough stock for product with id $productId available. $failureMode")
}
