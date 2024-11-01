package org.eda.ecommerce.integration

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.KafkaCompanionResource
import io.smallrye.common.annotation.Identifier
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eda.ecommerce.helpers.EntityHelper
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.models.Order.OrderStatus
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit


@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource::class)
class ShippingIntegrationTest {

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var shippingProducer: KafkaProducer<String, String>
    lateinit var consumer: KafkaConsumer<String, Order>

    @Inject
    lateinit var entityHelper: EntityHelper

    @Inject
    lateinit var orderRepository: OrderRepository

    @ConfigProperty(name = "test.eventing.assertion-timeout", defaultValue = "10")
    lateinit var timeoutInSeconds: String

    @BeforeEach
    fun setupKafkaHelpers() {
        shippingProducer = KafkaProducer(kafkaConfig, StringSerializer(), StringSerializer())
        consumer = KafkaTestHelper.setupConsumer<Order>(kafkaConfig)
    }

    @BeforeEach
    fun cleanRepositoryAndKafkaTopics() {
        entityHelper.clearAllRepositories()
    }

    @AfterEach
    fun tearDown() {
        shippingProducer.close()
        KafkaTestHelper.deleteConsumer(consumer)
    }

    @Test
    fun setOrderStatusWhenShippingSaidItWasDeliveredAndThrowEvent() {
        consumer.subscribe(listOf("order"))

        val order = entityHelper.createOrder()

        val shoppingBasketEvent: JsonObject = JsonObject()
            .put("shipmentId", UUID.randomUUID())
            .put("orderId", order.id)

        val productRecord = ProducerRecord<String, String>(
            "shipments",
            shoppingBasketEvent.encode()
        )
        productRecord.headers()
            .add("operation", "ShipmentDelivered".toByteArray())

        shippingProducer
            .send(productRecord)
            .get()

        await().atMost(timeoutInSeconds.toLong(), TimeUnit.SECONDS).untilAsserted {
            val foundOrder = orderRepository.findByIdWithRequestContext(order.id)

            assertEquals(OrderStatus.Fulfilled, foundOrder?.orderStatus)
        }

        // And expect event to be thrown
        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofSeconds(timeoutInSeconds.toLong()))

        assertEquals(1, records.count())

        val event = records.records("order").iterator().asSequence().toList().first()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })
        val eventPayload = event.value()

        assertEquals("order", eventHeaders["source"])
        assertEquals("fulfilled", eventHeaders["operation"])
        assertEquals(order.id.toString(), eventPayload.id.toString())
    }

}
