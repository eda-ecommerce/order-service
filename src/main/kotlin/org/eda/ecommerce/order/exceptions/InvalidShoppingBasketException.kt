package org.eda.ecommerce.order.exceptions

import java.util.*

class InvalidShoppingBasketException : EventProcessingException {
    constructor() : super("ShoppingBasket is not valid for order creation.")

    constructor(basketId: UUID) : super("ShoppingBasket $basketId is not valid for order creation.")

    constructor(
        basketId: UUID,
        failureMode: String
    ) : super("ShoppingBasket $basketId is not valid for order creation. $failureMode")
}
