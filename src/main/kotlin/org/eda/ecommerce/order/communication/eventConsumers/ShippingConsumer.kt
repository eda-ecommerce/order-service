package org.eda.ecommerce.order.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.order.data.events.external.incoming.operations.ShippingEventOperation
import org.eda.ecommerce.order.data.models.dto.ShipmentDTO
import org.eda.ecommerce.order.exceptions.EmptyEventPayloadException
import org.eda.ecommerce.order.services.OrderService

@ApplicationScoped
class ShippingConsumer {

    @Inject
    private lateinit var orderService: OrderService

    @Incoming("shipping-in")
    fun consume(record: ConsumerRecord<String, ShipmentDTO>) {
        val operation = String(record.headers().lastHeader("operation").value())
        println("Received Shipping event with operation: $operation")


        val shipping = record.value() ?: throw EmptyEventPayloadException(
            "Shopping Basket",
            operation
        )

        when (ShippingEventOperation.from(operation)) {
            ShippingEventOperation.DELIVERED -> orderService.handleDeliveryById(shipping.orderId)
        }
    }
}
