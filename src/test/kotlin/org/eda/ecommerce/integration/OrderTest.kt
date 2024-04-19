package org.eda.ecommerce.integration

import io.quarkus.hibernate.orm.panache.PanacheEntity_.id
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.InjectKafkaCompanion
import io.quarkus.test.kafka.KafkaCompanionResource
import io.restassured.RestAssured.given
import io.smallrye.common.annotation.Identifier
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.eda.ecommerce.data.models.Order
import org.eda.ecommerce.data.models.OrderStatus
import org.eda.ecommerce.data.models.ShoppingBasketItem
import org.eda.ecommerce.data.repositories.OrderRepository
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.junit.jupiter.api.*
import java.time.Duration
import java.util.*


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KafkaCompanionResource::class)
class OrderTest {

    @InjectKafkaCompanion
    lateinit var companion: KafkaCompanion

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var consumer: KafkaConsumer<String, Order>

    @Inject
    lateinit var orderRepository: OrderRepository

    val customerId = UUID.randomUUID()

    @BeforeEach
    @Transactional
    fun cleanRepositoryAndKafkaTopics() {
        KafkaTestHelper.clearTopicIfNotEmpty(companion, "order")
        consumer = KafkaTestHelper.setupConsumer<Order>(kafkaConfig)

        orderRepository.deleteAll()
    }

    @AfterEach
    fun unsubscribeConsumer() {
        KafkaTestHelper.deleteConsumer(consumer)
    }

    @Transactional
    fun createOrder () {
        val order = Order().apply {
            id = UUID.randomUUID();
            customerId = customerId;
            orderDate = Date().toString();
            orderStatus = OrderStatus.InProcess;
            totalPrice = 1.99F;
            items = mutableListOf<ShoppingBasketItem>()
                .plus(ShoppingBasketItem().apply {
                    shoppingBasketItemId = UUID.randomUUID();
                    quantity = 1;
                    totalPrice = 1.99F
                }).toMutableList()
        }

        this.orderRepository.persist(order)
    }

    @Test
    fun testCreationAndPersistenceWhenCreatingWithPost() {
        val orderDate = Date().toString()

        val jsonBody: JsonObject = JsonObject()
            .put("customerId", customerId)
            .put("orderDate", orderDate)
            .put("orderStatus", "InProcess")
            .put("totalPrice", 1.99F)
            .put("items", mutableListOf<ShoppingBasketItem>().plus(ShoppingBasketItem().apply { shoppingBasketItemId = UUID.randomUUID(); quantity = 1; totalPrice = 1.99F }))

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/order")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, orderRepository.count())

        val createdId = orderRepository.listAll()[0].id

        Assertions.assertEquals(OrderStatus.InProcess, orderRepository.findById(createdId).orderStatus)
        Assertions.assertEquals(jsonBody.getValue("customerId"), orderRepository.findById(createdId).customerId)
        Assertions.assertEquals(jsonBody.getValue("orderDate"), orderRepository.findById(createdId).orderDate)
        Assertions.assertEquals(jsonBody.getValue("totalPrice"), orderRepository.findById(createdId).totalPrice)
        Assertions.assertEquals(jsonBody.getJsonArray("items").size(), orderRepository.findById(createdId).items.size)
        Assertions.assertEquals(jsonBody.getJsonArray("items").getJsonObject(0).getValue("quantity"), orderRepository.findById(createdId).items[0].quantity)
        Assertions.assertEquals(jsonBody.getJsonArray("items").getJsonObject(0).getValue("totalPrice"), orderRepository.findById(createdId).items[0].totalPrice)
    }

    @Test
    fun testCreationAndPersistenceWithCancelledStatusWhenCreatingWithPost() {
        val orderDate = Date().toString()

        val jsonBody: JsonObject = JsonObject()
            .put("customerId", customerId)
            .put("orderDate", orderDate)
            .put("orderStatus", "Cancelled")
            .put("totalPrice", 1.99F)
            .put("items", mutableListOf<ShoppingBasketItem>().plus(ShoppingBasketItem().apply { shoppingBasketItemId = UUID.randomUUID(); quantity = 1; totalPrice = 1.99F }))

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/order")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, orderRepository.count())

        val createdId = orderRepository.listAll()[0].id

        Assertions.assertEquals(OrderStatus.Cancelled, orderRepository.findById(createdId).orderStatus)
        Assertions.assertEquals(jsonBody.getValue("customerId"), orderRepository.findById(createdId).customerId)
        Assertions.assertEquals(jsonBody.getValue("orderDate"), orderRepository.findById(createdId).orderDate)
        Assertions.assertEquals(jsonBody.getValue("totalPrice"), orderRepository.findById(createdId).totalPrice)
        Assertions.assertEquals(jsonBody.getJsonArray("items").size(), orderRepository.findById(createdId).items.size)
        Assertions.assertEquals(jsonBody.getJsonArray("items").getJsonObject(0).getValue("quantity"), orderRepository.findById(createdId).items[0].quantity)
        Assertions.assertEquals(jsonBody.getJsonArray("items").getJsonObject(0).getValue("totalPrice"), orderRepository.findById(createdId).items[0].totalPrice)
    }

    @Test
    fun testCreationAndPersistenceWithCompletedStatusWhenCreatingWithPost() {
        val orderDate = Date().toString()

        val jsonBody: JsonObject = JsonObject()
            .put("customerId", customerId)
            .put("orderDate", orderDate)
            .put("orderStatus", "Completed")
            .put("totalPrice", 1.99F)
            .put("items", mutableListOf<ShoppingBasketItem>().plus(ShoppingBasketItem().apply { shoppingBasketItemId = UUID.randomUUID(); quantity = 1; totalPrice = 1.99F }))

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/order")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, orderRepository.count())

        val createdId = orderRepository.listAll()[0].id

        Assertions.assertEquals(OrderStatus.Completed, orderRepository.findById(createdId).orderStatus)
        Assertions.assertEquals(jsonBody.getValue("customerId"), orderRepository.findById(createdId).customerId)
        Assertions.assertEquals(jsonBody.getValue("orderDate"), orderRepository.findById(createdId).orderDate)
        Assertions.assertEquals(jsonBody.getValue("totalPrice"), orderRepository.findById(createdId).totalPrice)
        Assertions.assertEquals(jsonBody.getJsonArray("items").size(), orderRepository.findById(createdId).items.size)
        Assertions.assertEquals(jsonBody.getJsonArray("items").getJsonObject(0).getValue("quantity"), orderRepository.findById(createdId).items[0].quantity)
        Assertions.assertEquals(jsonBody.getJsonArray("items").getJsonObject(0).getValue("totalPrice"), orderRepository.findById(createdId).items[0].totalPrice)
    }

    @Test
    fun testCreationAndPersistenceWithPaidStatusWhenCreatingWithPost() {
        val orderDate = Date().toString()

        val jsonBody: JsonObject = JsonObject()
            .put("customerId", customerId)
            .put("orderDate", orderDate)
            .put("orderStatus", "Paid")
            .put("totalPrice", 1.99F)
            .put("items", mutableListOf<ShoppingBasketItem>().plus(ShoppingBasketItem().apply { shoppingBasketItemId = UUID.randomUUID(); quantity = 1; totalPrice = 1.99F }))

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/order")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, orderRepository.count())

        val createdId = orderRepository.listAll()[0].id

        Assertions.assertEquals(OrderStatus.Paid, orderRepository.findById(createdId).orderStatus)
        Assertions.assertEquals(jsonBody.getValue("customerId"), orderRepository.findById(createdId).customerId)
        Assertions.assertEquals(jsonBody.getValue("orderDate"), orderRepository.findById(createdId).orderDate)
        Assertions.assertEquals(jsonBody.getValue("totalPrice"), orderRepository.findById(createdId).totalPrice)
        Assertions.assertEquals(jsonBody.getJsonArray("items").size(), orderRepository.findById(createdId).items.size)
        Assertions.assertEquals(jsonBody.getJsonArray("items").getJsonObject(0).getValue("quantity"), orderRepository.findById(createdId).items[0].quantity)
        Assertions.assertEquals(jsonBody.getJsonArray("items").getJsonObject(0).getValue("totalPrice"), orderRepository.findById(createdId).items[0].totalPrice)
    }


    @Test
    fun testKafkaEmitWhenCreatingWithPost() {
        consumer.subscribe(listOf("order"))

        val orderDate = Date().toString()

        val jsonBody: JsonObject = JsonObject()
            .put("customerId", customerId)
            .put("orderDate", orderDate)
            .put("orderStatus", "InProcess")
            .put("totalPrice", 1.99F)
            .put("items", mutableListOf<ShoppingBasketItem>().plus(ShoppingBasketItem().apply { shoppingBasketItemId = UUID.randomUUID(); quantity = 1; totalPrice = 1.99F }))

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/order")
            .then()
            .statusCode(201)

        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofMillis(10000))

        val event = records.records("order").iterator().asSequence().toList().first()
        val eventPayload = event.value()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })

        Assertions.assertEquals("order", eventHeaders["source"])
        Assertions.assertEquals("created", eventHeaders["operation"])
        Assertions.assertEquals(jsonBody.getValue("customerId"), eventPayload.customerId)
        Assertions.assertEquals(jsonBody.getValue("orderDate"), eventPayload.orderDate)
        Assertions.assertEquals(jsonBody.getValue("orderStatus"), eventPayload.orderStatus)
        Assertions.assertEquals(jsonBody.getValue("totalPrice"), eventPayload.totalPrice)
        Assertions.assertEquals(jsonBody.getJsonArray("items").size(), eventPayload.items.size)
        Assertions.assertEquals(jsonBody.getJsonArray("items").getJsonObject(0).getValue("quantity"), eventPayload.items[0].quantity)
        Assertions.assertEquals(jsonBody.getJsonArray("items").getJsonObject(0).getValue("totalPrice"), eventPayload.items[0].totalPrice)
    }

    @Test
    fun testUpdate() {
        consumer.subscribe(listOf("Order"))

        createOrder()
        Assertions.assertEquals(1, orderRepository.count())

        val createdId = orderRepository.listAll()[0].id

        val jsonBodyUpdated: JsonObject = JsonObject()
            .put("id", createdId)
            .put("customerId", customerId)
            .put("orderDate", Date().toString())
            .put("orderStatus", "Cancelled")
            .put("totalPrice", 1.99F)
            .put("items", mutableListOf<ShoppingBasketItem>().plus(ShoppingBasketItem().apply { quantity = 1; totalPrice = 1.99F }))

        given()
            .contentType("application/json")
            .body(jsonBodyUpdated.toString())
            .`when`()
            .put("/order")
            .then()
            .statusCode(204)

        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofMillis(10000))

        val event = records.records("Order").iterator().asSequence().toList().first()
        val eventPayload = event.value()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })

        Assertions.assertEquals("Order", eventHeaders["source"])
        Assertions.assertEquals("updated", eventHeaders["operation"])
        Assertions.assertEquals(createdId, eventPayload.id)
        Assertions.assertEquals(OrderStatus.Cancelled, eventPayload.orderStatus)
        Assertions.assertEquals(jsonBodyUpdated.getValue("customerId"), eventPayload.customerId)
        Assertions.assertEquals(jsonBodyUpdated.getValue("orderDate"), eventPayload.orderDate)
        Assertions.assertEquals(jsonBodyUpdated.getValue("totalPrice"), eventPayload.totalPrice)
        Assertions.assertEquals(jsonBodyUpdated.getJsonArray("items").size(), eventPayload.items.size)
        Assertions.assertEquals(jsonBodyUpdated.getJsonArray("items").getJsonObject(0).getValue("quantity"), eventPayload.items[0].quantity)
        Assertions.assertEquals(jsonBodyUpdated.getJsonArray("items").getJsonObject(0).getValue("totalPrice"), eventPayload.items[0].totalPrice)

        Assertions.assertEquals(1, orderRepository.count())
    }

    @Test
    fun testDelete() {
        consumer.subscribe(listOf("order"))

        createOrder()
        Assertions.assertEquals(1, orderRepository.count())

        val createdId = orderRepository.listAll()[0].id

        given()
            .contentType("application/json")
            .`when`()
            .delete("/Order/$createdId")
            .then()
            .statusCode(204)

        val records: ConsumerRecords<String, Order> = consumer.poll(Duration.ofMillis(10000))

        val event = records.records("Order").iterator().asSequence().toList().first()
        val eventPayload = event.value()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })

        Assertions.assertEquals("Order", eventHeaders["source"])
        Assertions.assertEquals("deleted", eventHeaders["operation"])
        Assertions.assertEquals(createdId, eventPayload.id)

        Assertions.assertEquals(0, orderRepository.count())
    }

}
