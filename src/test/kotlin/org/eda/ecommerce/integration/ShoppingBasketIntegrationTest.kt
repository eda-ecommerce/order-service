package org.eda.ecommerce.integration

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.InjectKafkaCompanion
import io.smallrye.common.annotation.Identifier
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.eda.ecommerce.helpers.CheckoutEventFactory
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.models.Order.OrderStatus
import org.eda.ecommerce.order.data.models.ShoppingBasketItem
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.eda.ecommerce.order.data.models.Offering
import org.eda.ecommerce.order.data.repositories.OfferingRepository
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

    @InjectKafkaCompanion
    lateinit var companion: KafkaCompanion


    lateinit var shoppingBasketProducer: KafkaProducer<String, String>
    lateinit var consumer: KafkaConsumer<String, Order>

    @Inject
    lateinit var orderRepository: OrderRepository

    @Inject
    lateinit var offeringRepository: OfferingRepository

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
        KafkaTestHelper.clearTopicIfNotEmpty(companion,"order")

        cleanupRepositories()
    }

    @Transactional
    fun cleanupRepositories() {
        orderRepository.deleteAll()
        offeringRepository.deleteAll()
    }

    @Transactional
    fun createOffering(pOfferingId: UUID, pProductId: UUID, pQuantity: Int) {
        val offering = Offering().apply {
            id = pOfferingId
            productId = pProductId
            quantity = pQuantity
        }

        offeringRepository.persist(offering)
    }

    @Test
    fun createOrderOnBasketSubmitAndExpectEvent() {
        consumer.subscribe(listOf("order"))

        val offeringId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val productsInOfferingCount = 2
        createOffering(offeringId, productId, productsInOfferingCount)

        val checkoutEventFactory = CheckoutEventFactory(customerId)
        val requestedOfferingCount = 2
        checkoutEventFactory.addOffering(offeringId, productsInOfferingCount)
        val (shoppingBasketEvent, basketId, totalPrice) = checkoutEventFactory.create()

        val shoppingBasketItemOne = shoppingBasketEvent.getJsonArray("items").getJsonObject(0)

        // This is the quantity for the one product in the offering.
        // It is the sum of all occurrences of this product in the basket across multiple possible offerings.
        val expectedProductQuantity = requestedOfferingCount * productsInOfferingCount

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
            Assertions.assertEquals(totalPrice, order.totalPrice)
            Assertions.assertEquals(requestedOfferingCount, order.totalItemQuantity)
            Assertions.assertEquals(1, order.items.size)
            Assertions.assertEquals(1, order.products.size)
            Assertions.assertEquals(productId, order.products.first().productId)
            Assertions.assertEquals(expectedProductQuantity, order.products.first().quantity)
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
        Assertions.assertEquals(totalPrice, eventPayload.totalPrice)
        Assertions.assertEquals(requestedOfferingCount, eventPayload.totalItemQuantity)
        Assertions.assertEquals(1, eventPayload.items.size)
        Assertions.assertEquals(1, eventPayload.products.size)
        Assertions.assertEquals(productId.toString(), eventPayload.products.first().productId.toString())
        Assertions.assertEquals(expectedProductQuantity, eventPayload.products.first().quantity)
        Assertions.assertEquals(shoppingBasketItemOne.getString("shoppingBasketItemId"), eventPayload.items.first().shoppingBasketItemId.toString())
        Assertions.assertEquals(shoppingBasketItemOne.getString("offeringId"), eventPayload.items.first().offeringId.toString())
        Assertions.assertEquals(shoppingBasketItemOne.getInteger("quantity"), eventPayload.items.first().quantity)
        Assertions.assertEquals(shoppingBasketItemOne.getFloat("totalPrice"), eventPayload.items.first().totalPrice)
        Assertions.assertEquals(ShoppingBasketItem.ItemState.AVAILABLE, eventPayload.items.first().itemState)
    }


    @Test
    fun dontAllowOrderCreationWhenBasketReferencesUnknownOffering() {
        consumer.subscribe(listOf("order"))

        val checkoutEventFactory = CheckoutEventFactory(customerId)
        // Add a random Offering to our checkout event that does not exist in the OfferingRepository
        checkoutEventFactory.addOffering()
        val (shoppingBasketEvent) = checkoutEventFactory.create()

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

        // Expect the order NOT to be created
        await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            Assertions.assertEquals(0, orderRepository.countWithRequestContext())
        }

        // And expect no event to be thrown
        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofMillis(10000))
        Assertions.assertEquals(0, records.count())
    }
}
