package org.eda.ecommerce.order.data.events.external.incoming

import org.eda.ecommerce.order.data.models.ShoppingBasket
import org.eda.ecommerce.order.data.events.external.incoming.EDAEvent

class ShoppingBasketEvent(source: Any, operation: Any, timestamp: String, payload: ShoppingBasket) : EDAEvent<ShoppingBasket>() {
    init {
        this.source = source as EventSource
        this.operation = operation as EventOperation
        this.timestamp = timestamp
        this.payload = payload
    }
}
