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
import org.eda.ecommerce.helpers.CheckoutEventFactory
import org.eda.ecommerce.helpers.EntityHelper
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.eda.ecommerce.order.data.models.Offering
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.models.StockEntry
import org.eda.ecommerce.order.data.repositories.OfferingRepository
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.eda.ecommerce.order.data.repositories.StockRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit


@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource::class)
class StockEntryIntegrationTest {

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var producer: KafkaProducer<String, String>
    lateinit var consumer: KafkaConsumer<String, Order>

    @Inject
    lateinit var entityHelper: EntityHelper

    @Inject
    lateinit var stockRepository: StockRepository

    @Inject
    lateinit var offeringRepository: OfferingRepository

    @Inject
    lateinit var orderRepository: OrderRepository

    @ConfigProperty(name = "test.eventing.assertion-timeout", defaultValue = "10")
    lateinit var timeoutInSeconds: String

    @BeforeEach
    fun setupKafkaHelpers() {
        producer = KafkaProducer(kafkaConfig, StringSerializer(), StringSerializer())
        consumer = KafkaTestHelper.setupConsumer<Order>(kafkaConfig)
    }

    @BeforeEach
    fun cleanRepositoryAndKafkaTopics() {
        entityHelper.clearAllRepositories()
    }

    @AfterEach
    fun tearDown() {
        producer.close()
        KafkaTestHelper.deleteConsumer(consumer)
    }

    @Test
    fun availableStockIsAdjustedWhenEventIsReceived() {
        val productId = UUID.randomUUID()

        val stockAdjustedEvent: JsonObject = JsonObject()
            .put("productId", productId)
            .put("actualStock", 10)
            .put("reservedStock", 3)
            .put("availableStock", 7)

        val productRecord = ProducerRecord<String, String>(
            "stock",
            stockAdjustedEvent.encode()
        )
        productRecord.headers()
            .add("operation", "AvailableStockAdjusted".toByteArray())

        producer
            .send(productRecord)
            .get()

        await().atMost(timeoutInSeconds.toLong(), TimeUnit.SECONDS).untilAsserted {
            assertEquals(1, stockRepository.countWithRequestContext())

            val foundStockEntry = stockRepository.getFirstWithRequestContext()

            assertEquals(7, foundStockEntry.availableStock)
        }
    }

    @Test
    fun orderIsNotAcceptedWhenAvailableStockIsNotEnough() {
        consumer.subscribe(listOf("order"))

        // We create a stock entry with 5 available stock
        val productId = UUID.randomUUID()
        entityHelper.createStockEntry(productId, 5)

        // We create an offering with 6 quantity of the product we only have 5 of
        val offering = entityHelper.createOffering(pProductId = productId, pQuantity = 6)

        val checkoutEventFactory = CheckoutEventFactory(UUID.randomUUID())

        // We add the offering to the checkout event once (i.e. we want to buy 6 * 1 = 6 of the product)
        checkoutEventFactory.addOffering(offering.id, 1)

        // Emit it
        val (shoppingBasketEvent) = checkoutEventFactory.create()
        val productRecord = ProducerRecord<String, String>(
            "shoppingBasket",
            shoppingBasketEvent.encode()
        )
        productRecord.headers()
            .add("operation", "checkout".toByteArray())
            .add("source", "shopping-basket-service".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())
        producer.send(productRecord)?.get()

        // Expect the order NOT to be created
        await().atMost(timeoutInSeconds.toLong(), TimeUnit.SECONDS).untilAsserted {
            assertEquals(0, orderRepository.countWithRequestContext())
        }

        // And expect no event to be thrown
        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofSeconds(timeoutInSeconds.toLong()))
        assertEquals(0, records.count())
    }

}
