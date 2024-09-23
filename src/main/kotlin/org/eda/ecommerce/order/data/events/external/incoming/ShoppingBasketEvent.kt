package org.eda.ecommerce.order.data.events.external.incoming

import org.eda.ecommerce.order.data.models.ShoppingBasket

class ShoppingBasketEvent(source: EventSource, operation: EventOperation, timestamp: String, payload: ShoppingBasket) : EDAEvent<ShoppingBasket>() {
    init {
        this.source = source
        this.operation = operation
        this.timestamp = timestamp
        this.payload = payload
    }
}
