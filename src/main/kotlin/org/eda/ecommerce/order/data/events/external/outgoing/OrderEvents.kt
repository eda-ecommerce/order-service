package org.eda.ecommerce.order.data.events.external.outgoing

import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.events.external.outgoing.KafkaCreatedMessage
import org.eda.ecommerce.order.data.events.external.outgoing.KafkaDeletedMessage
import org.eda.ecommerce.order.data.events.external.outgoing.KafkaUpdatedMessage

class OrderCreatedKafkaMessage(order: Order) : KafkaCreatedMessage<Order>(order)

class OrderUpdatedKafkaMessage(order: Order) : KafkaUpdatedMessage<Order>(order)

class OrderDeletedKafkaMessage(order: Order) : KafkaDeletedMessage<Order>(order)
