package org.eda.ecommerce.data.events.external.outgoing

import org.eda.ecommerce.data.models.Order

class OrderCreatedKafkaEvent(order: Order) : GenericKafkaCreatedEvent<Order>(order)

class OrderUpdatedKafkaEvent(order: Order) : GenericKafkaUpdatedEvent<Order>(order)

class OrderDeletedKafkaEvent(order: Order) : GenericKafkaDeletedEvent<Order>(order)
