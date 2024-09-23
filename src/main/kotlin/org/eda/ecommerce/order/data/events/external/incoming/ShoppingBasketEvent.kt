package org.eda.ecommerce.order.data.events.external.incoming

import org.eda.ecommerce.order.data.models.ShoppingBasket

class ShoppingBasketEvent(
    source: EventSource,
    operation: EventOperation,
    timestamp: String,
    payload: ShoppingBasket
) : EDAEvent<ShoppingBasket>(source, operation, timestamp, payload)
