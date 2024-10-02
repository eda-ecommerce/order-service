package org.eda.ecommerce.integration

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.KafkaCompanionResource
import io.smallrye.common.annotation.Identifier
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.models.Order.OrderStatus
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.junit.jupiter.api.*
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

    @Inject
    lateinit var orderRepository: OrderRepository

    val orderId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setupKafkaHelpers() {
        shippingProducer = KafkaProducer(kafkaConfig, StringSerializer(), StringSerializer())
    }

    @BeforeEach
    @Transactional
    fun cleanRepositoryAndKafkaTopics() {
        orderRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        shippingProducer.close()
    }

    @Transactional
    fun createOrder() {
        val order = Order().apply {
            id = orderId
        }

        orderRepository.persist(order)
    }

    @Test
    fun setOrderStatusWhenShippingSaidItWasDelivered() {
        createOrder()

        val shoppingBasketEvent: JsonObject = JsonObject()
            .put("ShippingId", UUID.randomUUID())
            .put("OrderId", orderId)
            .put("status", "delivered")

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

        await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            Assertions.assertEquals(1, orderRepository.countWithRequestContext())

            val order = orderRepository.findByIdWithRequestContext(orderId)

            Assertions.assertEquals(orderId, order?.id)
            Assertions.assertEquals(OrderStatus.Fulfilled, order?.orderStatus)
        }
    }

}
