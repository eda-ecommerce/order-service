package org.eda.ecommerce.integration

import io.quarkus.test.junit.QuarkusTest
import io.smallrye.common.annotation.Identifier
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.models.Order.OrderStatus
import org.eda.ecommerce.order.data.models.ShoppingBasketItem
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit


@QuarkusTest
class ShoppingBasketIntegrationTest {

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var shoppingBasketProducer: KafkaProducer<String, String>
    lateinit var consumer: KafkaConsumer<String, Order>

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
        consumer = KafkaTestHelper.setupConsumer<Order>(kafkaConfig)
    }

    @AfterEach
    fun tearDown() {
        shoppingBasketProducer.close()
        KafkaTestHelper.deleteConsumer(consumer)
    }

    @Test
    fun createOrderOnBasketSubmitAndExpectEvent() {
        consumer.subscribe(listOf("order"))

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
            Assertions.assertEquals(customerId, order.customerId)
            Assertions.assertEquals(1.99F, order.totalPrice)
            Assertions.assertEquals(1, order.totalItemQuantity)
            Assertions.assertEquals(1, order.items.size)
            Assertions.assertEquals(UUID.fromString(shoppingBasketItemOne.getString("shoppingBasketItemId")), order.items.first().shoppingBasketItemId)
            Assertions.assertEquals(UUID.fromString(shoppingBasketItemOne.getString("offeringId")), order.items.first().offeringId)
            Assertions.assertEquals(shoppingBasketItemOne.getInteger("quantity"), order.items.first().quantity)
            Assertions.assertEquals(shoppingBasketItemOne.getFloat("totalPrice"), order.items.first().totalPrice)
            Assertions.assertEquals(ShoppingBasketItem.ItemState.AVAILABLE, order.items.first().itemState)
        }

        // And expect event to be thrown
        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofMillis(10000))

        Assertions.assertEquals(1, records.count())

        val event = records.records("order").iterator().asSequence().toList().first()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })
        val eventPayload = event.value()

        Assertions.assertEquals("order", eventHeaders["source"])
        Assertions.assertEquals("created", eventHeaders["operation"])
        Assertions.assertEquals(basketId.toString(), eventPayload.shoppingBasketId.toString())
        Assertions.assertEquals(OrderStatus.InProcess, eventPayload.orderStatus)
        Assertions.assertEquals(customerId.toString(), eventPayload.customerId.toString())
        Assertions.assertEquals(1.99F, eventPayload.totalPrice)
        Assertions.assertEquals(1, eventPayload.totalItemQuantity)
        Assertions.assertEquals(1, eventPayload.items.size)
        Assertions.assertEquals(shoppingBasketItemOne.getString("shoppingBasketItemId"), eventPayload.items.first().shoppingBasketItemId.toString())
        Assertions.assertEquals(shoppingBasketItemOne.getString("offeringId"), eventPayload.items.first().offeringId.toString())
        Assertions.assertEquals(shoppingBasketItemOne.getInteger("quantity"), eventPayload.items.first().quantity)
        Assertions.assertEquals(shoppingBasketItemOne.getFloat("totalPrice"), eventPayload.items.first().totalPrice)
        Assertions.assertEquals(ShoppingBasketItem.ItemState.AVAILABLE, eventPayload.items.first().itemState)
    }

}
