package org.eda.ecommerce.integration

import io.quarkus.test.junit.QuarkusTest
import io.smallrye.common.annotation.Identifier
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.eda.ecommerce.data.models.OrderStatus
import org.eda.ecommerce.data.repositories.OrderRepository
import org.junit.jupiter.api.*
import java.util.*
import java.util.concurrent.TimeUnit


@QuarkusTest
class ShoppingBasketIntegrationTest {

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var shoppingBasketProducer: KafkaProducer<String, String>

    @Inject
    lateinit var orderRepository: OrderRepository

    val customerId: UUID = UUID.randomUUID()

    @BeforeEach
    @Transactional
    fun cleanRepositoryAndKafkaTopics() {
        orderRepository.deleteAll()
    }

    @BeforeEach
    fun setupKafkaHelpers() {
        shoppingBasketProducer = KafkaProducer(kafkaConfig, StringSerializer(), StringSerializer())
    }

    @AfterEach
    fun tearDown() {
        shoppingBasketProducer.close()
    }

    @Test
    fun createOrderOnBasketSubmit() {
        val basketId = UUID.randomUUID()

        val shoppingBasketItemOne: JsonObject = JsonObject()
            .put("shoppingBasketItemId", UUID.randomUUID())
            .put("shoppingBasketId", basketId)
            .put("offeringId", UUID.randomUUID())
            .put("quantity", 1)
            .put("totalPrice", 1.99F)
            .put("itemState", "AVAILABLE")

        val shoppingBasketItems: JsonArray = JsonArray()
            .add(shoppingBasketItemOne)

        val shoppingBasketEvent: JsonObject = JsonObject()
            .put("shoppingBasketId", basketId)
            .put("customerId", customerId)
            .put("totalPrice", 1.99F)
            .put("totalItemQuantity", 1)
            .put("items", shoppingBasketItems)


        println(shoppingBasketEvent.encode())

        val productRecord = ProducerRecord<String, String>(
            "shoppingBasket",
            shoppingBasketEvent.encode()
        )
        productRecord.headers()
            .add("operation", "CHECKOUT".toByteArray())
            .add("source", "shopping-basket-service".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        shoppingBasketProducer
            .send(productRecord)
            .get()

        await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            Assertions.assertEquals(1, orderRepository.countWithRequestContext())

            val order = orderRepository.getFirstWithRequestContext()

            Assertions.assertEquals(basketId, order.shoppingBasketId)
            Assertions.assertEquals(OrderStatus.InProcess, order.orderStatus)
        }
    }

}
