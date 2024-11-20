package org.eda.ecommerce.order.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.order.data.events.external.incoming.operations.OfferingEventOperation
import org.eda.ecommerce.order.data.models.dto.OfferingDTO
import org.eda.ecommerce.order.exceptions.EmptyEventPayloadException
import org.eda.ecommerce.order.services.OfferingService

@ApplicationScoped
class OfferingConsumer {

    @Inject
    private lateinit var offeringService: OfferingService

    @Incoming("offering-in")
    @Transactional
    fun consume(record: ConsumerRecord<String, OfferingDTO>) {
        val operation = String(record.headers().lastHeader("operation").value())
        println("Received Offering event with operation '$operation' and payload: ${record.value()}")

        val offeringDTO = record.value() ?: throw EmptyEventPayloadException(
            "Shopping Basket",
            operation
        )

        val offering = offeringDTO.toEntity()

        when (OfferingEventOperation.from(operation)) {
            OfferingEventOperation.CREATED -> offeringService.saveOffering(offering)
            OfferingEventOperation.UPDATED -> offeringService.updateOffering(offering)
        }
    }

}
