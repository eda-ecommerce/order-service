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
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eda.ecommerce.helpers.CheckoutEventFactory
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.eda.ecommerce.order.data.models.Offering
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.models.Order.OrderStatus
import org.eda.ecommerce.order.data.models.ShoppingBasketItem
import org.eda.ecommerce.order.data.repositories.OfferingRepository
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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

    @ConfigProperty(name = "test.eventing.assertion-timeout", defaultValue = "10")
    lateinit var timeoutInSeconds: String

    val customerId: UUID = UUID.randomUUID()

    @BeforeEach
    @Transactional
    fun cleanRepositoryAndKafkaTopics() {
        orderRepository.deleteAll()
        offeringRepository.deleteAll()
        KafkaTestHelper.clearTopicIfNotEmpty(companion,"order")
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

    @Transactional
    fun createOffering(pOfferingId: UUID? = null, pProductId: UUID? = null, pQuantity: Int = 1, pStatus: Offering.OfferingStatus = Offering.OfferingStatus.ACTIVE): Offering {
        val offering = Offering().apply {
            id = pOfferingId ?: UUID.randomUUID()
            productId = pProductId ?: UUID.randomUUID()
            quantity = pQuantity
            status = pStatus
        }

        offeringRepository.persist(offering)

        println("Created offering: $offering")

        return offering
    }

    fun ensureOfferingIsUpdatable(pOffering: Offering) : Offering {
        if (!offeringRepository.isPersistent(pOffering)) {
            println("Offering to update is not persistent. Refreshing from repository with ID: ${pOffering.id}")
            return offeringRepository.findById(pOffering.id)
        }

        return pOffering
    }

    @Transactional
    fun updateOfferingStatus(pOffering: Offering, pStatus: Offering.OfferingStatus) {
        val offering = ensureOfferingIsUpdatable(pOffering)

        println("Updating status of offering $offering to $pStatus")

        offering.status = pStatus
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
            .add("operation", "checkout".toByteArray())
            .add("source", "shopping-basket-service".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        shoppingBasketProducer.send(productRecord)?.get()

        await().atMost(timeoutInSeconds.toLong(), TimeUnit.SECONDS).untilAsserted {
            assertEquals(1, orderRepository.countWithRequestContext())

            val order = orderRepository.getFirstWithRequestContext()

            assertEquals(basketId, order.shoppingBasketId)
            assertEquals(OrderStatus.Requested, order.orderStatus)
            assertEquals(customerId, order.customerId)
            assertEquals(totalPrice, order.totalPrice)
            assertEquals(requestedOfferingCount, order.totalItemQuantity)
            assertEquals(1, order.items.size)
            assertEquals(1, order.products.size)
            assertEquals(productId, order.products.first().productId)
            assertEquals(expectedProductQuantity, order.products.first().quantity)
            assertEquals(UUID.fromString(shoppingBasketItemOne.getString("shoppingBasketItemId")), order.items.first().shoppingBasketItemId)
            assertEquals(UUID.fromString(shoppingBasketItemOne.getString("offeringId")), order.items.first().offeringId)
            assertEquals(shoppingBasketItemOne.getInteger("quantity"), order.items.first().quantity)
            assertEquals(shoppingBasketItemOne.getFloat("totalPrice"), order.items.first().totalPrice)
            assertEquals(ShoppingBasketItem.ItemState.AVAILABLE, order.items.first().itemState)
        }

        // And expect event to be thrown
        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofSeconds(timeoutInSeconds.toLong()))

        assertEquals(1, records.count())

        val event = records.records("order").iterator().asSequence().toList().first()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })
        val eventPayload = event.value()

        assertEquals("order", eventHeaders["source"])
        assertEquals("requested", eventHeaders["operation"])
        assertEquals(basketId.toString(), eventPayload.shoppingBasketId.toString())
        assertEquals(OrderStatus.Requested, eventPayload.orderStatus)
        assertEquals(customerId.toString(), eventPayload.customerId.toString())
        assertEquals(totalPrice, eventPayload.totalPrice)
        assertEquals(requestedOfferingCount, eventPayload.totalItemQuantity)
        assertEquals(1, eventPayload.items.size)
        assertEquals(1, eventPayload.products.size)
        assertEquals(productId.toString(), eventPayload.products.first().productId.toString())
        assertEquals(expectedProductQuantity, eventPayload.products.first().quantity)
        assertEquals(shoppingBasketItemOne.getString("shoppingBasketItemId"), eventPayload.items.first().shoppingBasketItemId.toString())
        assertEquals(shoppingBasketItemOne.getString("offeringId"), eventPayload.items.first().offeringId.toString())
        assertEquals(shoppingBasketItemOne.getInteger("quantity"), eventPayload.items.first().quantity)
        assertEquals(shoppingBasketItemOne.getFloat("totalPrice"), eventPayload.items.first().totalPrice)
        assertEquals(ShoppingBasketItem.ItemState.AVAILABLE, eventPayload.items.first().itemState)
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
            .add("operation", "checkout".toByteArray())
            .add("source", "shopping-basket-service".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        shoppingBasketProducer.send(productRecord)?.get()

        // Expect the order NOT to be created
        await().atMost(timeoutInSeconds.toLong(), TimeUnit.SECONDS).untilAsserted {
            assertEquals(0, orderRepository.countWithRequestContext())
        }

        // And expect no event to be thrown
        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofSeconds(timeoutInSeconds.toLong()))
        assertEquals(0, records.count())
    }

    @Test
    fun dontAllowOrderCreationWhenBasketReferencesRetiredOffering() {
        consumer.subscribe(listOf("order"))

        val offeringId = UUID.randomUUID()
        val offering = createOffering(offeringId)

        // Now retire the offering
        updateOfferingStatus(offering, Offering.OfferingStatus.RETIRED)

        val checkoutEventFactory = CheckoutEventFactory(customerId)
        // Add a random Offering to our checkout event that does not exist in the OfferingRepository
        checkoutEventFactory.addOffering(offeringId)
        val (shoppingBasketEvent) = checkoutEventFactory.create()

        val productRecord = ProducerRecord<String, String>(
            "shoppingBasket",
            shoppingBasketEvent.encode()
        )
        productRecord.headers()
            .add("operation", "checkout".toByteArray())
            .add("source", "shopping-basket-service".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        shoppingBasketProducer.send(productRecord)?.get()

        // Expect the order NOT to be created
        await().atMost(timeoutInSeconds.toLong(), TimeUnit.SECONDS).untilAsserted {
            assertEquals(0, orderRepository.countWithRequestContext())
        }

        // And expect no event to be thrown
        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofSeconds(timeoutInSeconds.toLong()))
        assertEquals(0, records.count())
    }

    @Test
    fun dontAllowOrderCreationWhenBasketReferencesInactiveOffering() {
        consumer.subscribe(listOf("order"))

        val offeringId = UUID.randomUUID()
        val offering = createOffering(offeringId)

        // Now set the offering to inactive
        updateOfferingStatus(offering, Offering.OfferingStatus.INACTIVE)

        val checkoutEventFactory = CheckoutEventFactory(customerId)
        // Add a random Offering to our checkout event that does not exist in the OfferingRepository
        checkoutEventFactory.addOffering(offeringId)
        val (shoppingBasketEvent) = checkoutEventFactory.create()

        val productRecord = ProducerRecord<String, String>(
            "shoppingBasket",
            shoppingBasketEvent.encode()
        )
        productRecord.headers()
            .add("operation", "checkout".toByteArray())
            .add("source", "shopping-basket-service".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        shoppingBasketProducer.send(productRecord)?.get()

        // Expect the order NOT to be created
        await().atMost(timeoutInSeconds.toLong(), TimeUnit.SECONDS).untilAsserted {
            assertEquals(0, orderRepository.countWithRequestContext())
        }

        // And expect no event to be thrown
        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofSeconds(timeoutInSeconds.toLong()))
        assertEquals(0, records.count())
    }
}
