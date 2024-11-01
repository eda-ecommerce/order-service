package org.eda.ecommerce.order.exceptions

import java.util.*

class NoStockRecordsForProductFoundException : EventProcessingException {
    constructor() : super("StockEntry not found")
    constructor(productId: UUID) : super("StockEntry for product with id $productId not found.")

    constructor(productId: UUID, failureMode: String) : super("StockEntry product with id $productId not found. $failureMode")
}
