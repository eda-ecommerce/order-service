package org.eda.ecommerce.order.data.events.external.outgoing

import org.eda.ecommerce.order.data.models.Order

enum class OrderOperations(override val value: String) : Operation {
    REQUESTED("requested"),
    CONFIRMED("confirmed"),
    FULFILLED("fulfilled"),
    CANCELLED("cancelled")
}

class OrderRequestedKafkaMessage(order: Order) : KafkaMessage<Order>(OrderOperations.REQUESTED, order)

class OrderConfirmedKafkaMessage(order: Order) : KafkaMessage<Order>(OrderOperations.CONFIRMED, order)

class OrderFulfilledKafkaMessage(order: Order) : KafkaMessage<Order>(OrderOperations.FULFILLED, order)

class OrderCancelledKafkaMessage(order: Order) : KafkaMessage<Order>(OrderOperations.CANCELLED, order)
