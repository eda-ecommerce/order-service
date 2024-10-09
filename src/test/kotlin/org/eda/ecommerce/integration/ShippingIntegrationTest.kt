package org.eda.ecommerce.integration

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.KafkaCompanionResource
import io.smallrye.common.annotation.Identifier
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.models.Order.OrderStatus
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KafkaCompanionResource::class)
class ShippingIntegrationTest {

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var shippingProducer: KafkaProducer<String, String>
    lateinit var consumer: KafkaConsumer<String, Order>

    @Inject
    lateinit var orderRepository: OrderRepository

    @ConfigProperty(name = "test.eventing.assertion-timeout", defaultValue = "10")
    lateinit var timeoutInSeconds: String

    val orderId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setupKafkaHelpers() {
        shippingProducer = KafkaProducer(kafkaConfig, StringSerializer(), StringSerializer())
        consumer = KafkaTestHelper.setupConsumer<Order>(kafkaConfig)
    }

    @BeforeEach
    @Transactional
    fun cleanRepositoryAndKafkaTopics() {
        orderRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        shippingProducer.close()
        KafkaTestHelper.deleteConsumer(consumer)
    }

    @Transactional
    fun createOrder() {
        val order = Order().apply {
            id = orderId
        }

        orderRepository.persist(order)
    }

    @Test
    fun setOrderStatusWhenShippingSaidItWasDeliveredAndThrowEvent() {
        consumer.subscribe(listOf("order"))

        createOrder()

        val shoppingBasketEvent: JsonObject = JsonObject()
            .put("shipmentId", UUID.randomUUID())
            .put("orderId", orderId)

        val productRecord = ProducerRecord<String, String>(
            "shipping",
            shoppingBasketEvent.encode()
        )
        productRecord.headers()
            .add("operation", "updated".toByteArray())
            .add("source", "shipping".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        shippingProducer
            .send(productRecord)
            .get()

        await().atMost(timeoutInSeconds.toLong(), TimeUnit.SECONDS).untilAsserted {
            assertEquals(1, orderRepository.countWithRequestContext())

            val order = orderRepository.findByIdWithRequestContext(orderId)

            assertEquals(orderId, order?.id)
            assertEquals(OrderStatus.Fulfilled, order?.orderStatus)
        }

        // And expect event to be thrown
        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofMillis(10000))

        assertEquals(1, records.count())

        val event = records.records("order").iterator().asSequence().toList().first()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })
        val eventPayload = event.value()

        assertEquals("order", eventHeaders["source"])
        assertEquals("requested", eventHeaders["operation"])
        assertEquals(orderId.toString(), eventPayload.id.toString())
    }

}
