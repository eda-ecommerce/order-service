package org.eda.ecommerce.order.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.order.data.models.Offering
import org.eda.ecommerce.order.services.OfferingService

@ApplicationScoped
class OfferingConsumer {

    @Inject
    private lateinit var offeringService: OfferingService

    @Incoming("offering-in")
    @Transactional
    fun consume(record: ConsumerRecord<String, Offering>) {
        val operation = record.headers().lastHeader("operation")
        println("Received Offering event with operation '${String(operation.value())}' for Offering: ${record.value()}")

        when (String(operation.value())) {
            "created" -> offeringService.saveOffering(record.value())
            "updated" -> offeringService.updateOffering(record.value())
        }
    }

}
