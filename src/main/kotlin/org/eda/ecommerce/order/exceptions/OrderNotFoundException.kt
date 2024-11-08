package org.eda.ecommerce.order.exceptions

import java.util.*

class OrderNotFoundException : EventProcessingException {
    constructor() : super("Order not found")

    constructor(orderId: UUID) : super("Order with id $orderId not found")

    constructor(orderId: UUID, failureMode: String) : super("Order with id $orderId not found. $failureMode")
}
