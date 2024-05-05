package org.eda.ecommerce.data.events.external.incoming

import jakarta.persistence.Entity
import org.eda.ecommerce.data.models.ShoppingBasket

@Entity
class ShoppingBasketEvent() : StorableKafkaEvent<ShoppingBasket>() {
    constructor(operation: String, source: String, timestamp: String, payload: ShoppingBasket) : this() {
        this.operation = operation
        this.source = EventSource.from(source)
        this.timestamp = timestamp
        this.payload = payload
    }
}
