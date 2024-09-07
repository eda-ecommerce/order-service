package org.eda.ecommerce.data.events.external.incoming

import org.eda.ecommerce.data.models.ShoppingBasket

class ShoppingBasketEvent(source: Any, operation: Any, timestamp: String, payload: ShoppingBasket) : EDAEvent<ShoppingBasket>() {
    init {
        this.source = source as EventSource
        this.operation = operation as EventOperation
        this.timestamp = timestamp
        this.payload = payload
    }
}
