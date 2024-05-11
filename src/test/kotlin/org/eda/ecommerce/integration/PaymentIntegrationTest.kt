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
import org.eda.ecommerce.data.models.Order
import org.eda.ecommerce.data.models.Order.OrderStatus
import org.eda.ecommerce.data.repositories.OrderRepository
import org.junit.jupiter.api.*
import java.util.*
import java.util.concurrent.TimeUnit


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KafkaCompanionResource::class)
class PaymentIntegrationTest {

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var paymentProducer: KafkaProducer<String, String>

    @Inject
    lateinit var orderRepository: OrderRepository

    val orderId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setupKafkaHelpers() {
        paymentProducer = KafkaProducer(kafkaConfig, StringSerializer(), StringSerializer())
    }

    @BeforeEach
    @Transactional
    fun cleanRepositoryAndKafkaTopics() {
        orderRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        paymentProducer.close()
    }

    @Transactional
    fun createOrder() {
        val order = Order().apply {
            id = orderId
        }

        orderRepository.persist(order)
    }

    @Test
    fun setOrderStatusWhenPaymentSaidItWasPaid() {
        createOrder()

        val shoppingBasketEvent: JsonObject = JsonObject()
            .put("PaymentId", UUID.randomUUID())
            .put("OrderId", orderId)
            .put("PaymentDate", Date().toString())
            .put("CreatedDate", Date().toString())
            .put("status", "paid")

        val productRecord = ProducerRecord<String, String>(
            "payment",
            shoppingBasketEvent.encode()
        )
        productRecord.headers()
            .add("operation", "updated".toByteArray())
            .add("source", "payment".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        paymentProducer
            .send(productRecord)
            .get()

        await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            Assertions.assertEquals(1, orderRepository.countWithRequestContext())

            val order = orderRepository.findByIdWithRequestContext(orderId)

            Assertions.assertEquals(orderId, order?.id)
            Assertions.assertEquals(OrderStatus.Paid, order?.orderStatus)
        }
    }

}
