package org.eda.ecommerce.data.events.external.outgoing

import org.eda.ecommerce.data.models.Order

class OrderCreatedKafkaMessage(order: Order) : KafkaCreatedMessage<Order>(order)

class OrderUpdatedKafkaMessage(order: Order) : KafkaUpdatedMessage<Order>(order)

class OrderDeletedKafkaMessage(order: Order) : KafkaDeletedMessage<Order>(order)
