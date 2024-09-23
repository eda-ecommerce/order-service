package org.eda.ecommerce.order.data.events.external.outgoing

import org.eda.ecommerce.order.data.models.Order

class OrderCreatedKafkaMessage(order: Order) : KafkaCreatedMessage<Order>(order)

class OrderUpdatedKafkaMessage(order: Order) : KafkaUpdatedMessage<Order>(order)

class OrderDeletedKafkaMessage(order: Order) : KafkaDeletedMessage<Order>(order)
