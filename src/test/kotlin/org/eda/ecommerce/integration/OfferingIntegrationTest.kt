package org.eda.ecommerce.integration

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.KafkaCompanionResource
import io.smallrye.common.annotation.Identifier
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eda.ecommerce.helpers.EntityHelper
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.eda.ecommerce.order.data.models.Offering
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.repositories.OfferingRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.TimeUnit


@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource::class)
class OfferingIntegrationTest {

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var producer: KafkaProducer<String, String>
    lateinit var consumer: KafkaConsumer<String, Order>

    @Inject
    lateinit var entityHelper: EntityHelper

    @Inject
    lateinit var offeringRepository: OfferingRepository

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
    fun offeringSavedOnCreatedEvent() {
        val offeringId = UUID.randomUUID()
        val productId = UUID.randomUUID()

        val product : JsonObject = JsonObject()
            .put("id", productId)
            .put("status", "active")

        val stockAdjustedEvent: JsonObject = JsonObject()
            .put("id", offeringId)
            .put("product", product)
            .put("quantity", 1)
            .put("status", "active")

        val offeringRecord = ProducerRecord<String, String>(
            "offering",
            stockAdjustedEvent.encode()
        )
        offeringRecord.headers()
            .add("operation", "created".toByteArray())

        producer
            .send(offeringRecord)
            .get()

        await().atMost(timeoutInSeconds.toLong(), TimeUnit.SECONDS).untilAsserted {
            assertEquals(1, offeringRepository.countWithRequestContext())

            val foundOfferingEntry = offeringRepository.getFirstWithRequestContext()

            assertEquals(offeringId, foundOfferingEntry.id)
            assertEquals(productId, foundOfferingEntry.productId)
            assertEquals(1, foundOfferingEntry.quantity)
            assertEquals(Offering.OfferingStatus.ACTIVE, foundOfferingEntry.status)
        }
    }

    @Test
    fun offeringUpdatedOnUpdateEvent() {
        val offeringId = UUID.randomUUID()
        val productId = UUID.randomUUID()

        entityHelper.createOffering(
            offeringId,
            productId,
            1,
            Offering.OfferingStatus.ACTIVE
        )

        val product : JsonObject = JsonObject()
            .put("id", productId)
            .put("status", "active")

        val stockAdjustedEvent: JsonObject = JsonObject()
            .put("id", offeringId)
            .put("product", product)
            .put("quantity", 2)
            .put("status", "inactive")

        val offeringRecord = ProducerRecord<String, String>(
            "offering",
            stockAdjustedEvent.encode()
        )
        offeringRecord.headers()
            .add("operation", "updated".toByteArray())

        producer
            .send(offeringRecord)
            .get()

        await().atMost(timeoutInSeconds.toLong(), TimeUnit.SECONDS).untilAsserted {
            assertEquals(1, offeringRepository.countWithRequestContext())

            val foundOfferingEntry = offeringRepository.getFirstWithRequestContext()

            assertEquals(offeringId, foundOfferingEntry.id)
            assertEquals(productId, foundOfferingEntry.productId)
            assertEquals(2, foundOfferingEntry.quantity)
            assertEquals(Offering.OfferingStatus.INACTIVE, foundOfferingEntry.status)
        }
    }


}
