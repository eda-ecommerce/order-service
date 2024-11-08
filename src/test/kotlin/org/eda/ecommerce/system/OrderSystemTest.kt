package org.eda.ecommerce.system

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.InjectKafkaCompanion
import io.quarkus.test.kafka.KafkaCompanionResource
import io.restassured.RestAssured.given
import io.smallrye.common.annotation.Identifier
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eda.ecommerce.helpers.EntityHelper
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.data.repositories.OrderRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*


@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource::class)
class OrderSystemTest {

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var consumer: KafkaConsumer<String, Order>

    @InjectKafkaCompanion
    lateinit var companion: KafkaCompanion

    @Inject
    lateinit var entityHelper: EntityHelper

    @Inject
    lateinit var orderRepository: OrderRepository

    @ConfigProperty(name = "test.eventing.assertion-timeout", defaultValue = "10")
    lateinit var timeoutInSeconds: String

    @BeforeEach
    fun setupKafkaHelpers() {
        consumer = KafkaTestHelper.setupConsumer<Order>(kafkaConfig)
    }

    @BeforeEach
    fun cleanRepositoryAndKafkaTopics() {
        entityHelper.clearAllRepositories()
        KafkaTestHelper.clearTopicIfNotEmpty(companion, "order")
    }

    @AfterEach
    fun tearDown() {
        KafkaTestHelper.deleteConsumer(consumer)
    }

    @Test
    fun confirmationOfOrderSetsStatusAndThrowsEvent() {
        consumer.subscribe(listOf("order"))

        val createdOrder = entityHelper.createOrder()

        given()
            .contentType("application/json")
            .`when`().post("/order/${createdOrder.id}/confirm")
            .then()
            .statusCode(200)

        assertEquals(1, orderRepository.countWithRequestContext())

        val order = orderRepository.getFirstWithRequestContext()

        assertEquals(createdOrder.id, order.id)
        assertEquals(Order.OrderStatus.Confirmed, order.orderStatus)

        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofSeconds(timeoutInSeconds.toLong()))

        assertEquals(1, records.count())

        val event = records.records("order").iterator().asSequence().toList().first()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })
        val eventPayload = event.value()

        assertEquals("confirmed", eventHeaders["operation"])
        assertEquals(Order.OrderStatus.Confirmed, eventPayload.orderStatus)
    }

    @Test
    fun confirmationNotAcceptedForNonexistingId() {
        given()
            .contentType("application/json")
            .`when`().post("/order/${UUID.randomUUID()}/confirm")
            .then()
            .statusCode(500)
    }

    @Test
    fun cancellationOfOrderSetsStatusAndThrowsEvent() {
        consumer.subscribe(listOf("order"))

        val createdOrder = entityHelper.createOrder()

        given()
            .contentType("application/json")
            .`when`().post("/order/${createdOrder.id}/cancel")
            .then()
            .statusCode(200)

        assertEquals(1, orderRepository.countWithRequestContext())

        val order = orderRepository.getFirstWithRequestContext()

        assertEquals(createdOrder.id, order.id)
        assertEquals(Order.OrderStatus.Canceled, order.orderStatus)

        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofSeconds(timeoutInSeconds.toLong()))

        assertEquals(1, records.count())

        val event = records.records("order").iterator().asSequence().toList().first()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })
        val eventPayload = event.value()

        assertEquals("cancelled", eventHeaders["operation"])
        assertEquals(Order.OrderStatus.Canceled, eventPayload.orderStatus)
    }

    @Test
    fun cancellationNotAcceptedForNonexistingId() {
        given()
            .contentType("application/json")
            .`when`().post("/order/${UUID.randomUUID()}/cancel")
            .then()
            .statusCode(500)
    }

}
