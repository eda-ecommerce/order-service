package org.eda.ecommerce.order.exceptions

import java.util.*

class OrderCancelledException : EventProcessingException {
    constructor() : super("Order cancelled. No further processing possible.")

    constructor(orderId: UUID) : super("Order with id $orderId cancelled. No further processing possible.")

    constructor(orderId: UUID, failureMode: String) : super("Order with id $orderId cancelled. No further processing possible.. $failureMode")
}
