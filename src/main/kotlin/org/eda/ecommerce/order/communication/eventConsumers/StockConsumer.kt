package org.eda.ecommerce.order.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.order.data.events.external.incoming.operations.StockEventOperation
import org.eda.ecommerce.order.data.models.dto.StockDTO
import org.eda.ecommerce.order.exceptions.EmptyEventPayloadException
import org.eda.ecommerce.order.services.StockService

@ApplicationScoped
class StockConsumer {

    @Inject
    private lateinit var stockService: StockService

    @Incoming("stock-in")
    fun consume(record: ConsumerRecord<String, StockDTO>) {
        val operation = String(record.headers().lastHeader("operation").value())
        println("Received stock event with operation: $operation")


        val stockEvent = record.value() ?: throw EmptyEventPayloadException(
            "Stock",
            operation
        )

        when (StockEventOperation.from(operation)) {
            StockEventOperation.ADJUSTED -> stockService.adjustStock(stockEvent.productId, stockEvent.availableStock)
        }
    }
}
