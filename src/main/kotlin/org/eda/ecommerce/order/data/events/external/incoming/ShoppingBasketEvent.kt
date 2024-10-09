package org.eda.ecommerce.order.data.events.external.incoming

import org.eda.ecommerce.order.data.events.external.incoming.operations.ShoppingBasketEventOperation
import org.eda.ecommerce.order.data.models.ShoppingBasket

class ShoppingBasketEvent(
    source: EventSource,
    operation: ShoppingBasketEventOperation,
    timestamp: String,
    payload: ShoppingBasket
) : EDAEvent<ShoppingBasket>(source, operation, timestamp, payload)
