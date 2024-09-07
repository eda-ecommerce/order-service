package org.eda.ecommerce.helpers

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.*

class CheckoutEventFactory(private val customerId: UUID) {

    private val shoppingBasketItems = JsonArray()

    fun addOffering(offeringId: UUID? = null, quantity: Int? = 1, price: Float = 1.99F) {
        val shoppingBasketItem = JsonObject()
            .put("shoppingBasketItemId", UUID.randomUUID())
            .put("shoppingBasketId", UUID.randomUUID())
            .put("offeringId", offeringId ?: UUID.randomUUID())
            .put("quantity", quantity ?: 1)
            .put("totalPrice", price * (quantity ?: 1))
            .put("itemState", "AVAILABLE")

        shoppingBasketItems.add(shoppingBasketItem)
    }

    fun create(): Triple<JsonObject, UUID, Float> {
        val basketId = UUID.randomUUID()

        // Sum totalPrice of all shoppingBasketItems
        val totalPrice = shoppingBasketItems.stream()
            .map { it as JsonObject }
            .map { it.getFloat("totalPrice") }
            .reduce { acc, price -> acc + price }
            .orElse(0F)

        // Sum totalItemQuantity of all shoppingBasketItems
        val totalItemQuantity = shoppingBasketItems.stream()
            .map { it as JsonObject }
            .map { it.getInteger("quantity") }
            .reduce { acc, quantity -> acc + quantity }
            .orElse(0)

        val shoppingBasketEvent = JsonObject()
            .put("shoppingBasketId", basketId)
            .put("customerId", customerId)
            .put("totalPrice", totalPrice)
            .put("totalItemQuantity", totalItemQuantity)
            .put("items", shoppingBasketItems)

        return Triple(shoppingBasketEvent, basketId, totalPrice)
    }
}
