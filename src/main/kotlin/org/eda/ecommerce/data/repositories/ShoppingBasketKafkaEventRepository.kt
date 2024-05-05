package org.eda.ecommerce.data.repositories

import jakarta.enterprise.context.ApplicationScoped
import org.eda.ecommerce.data.events.external.incoming.ShoppingBasketEvent
import org.eda.ecommerce.data.models.ShoppingBasket

@ApplicationScoped
class ShoppingBasketKafkaEventRepository :
    GenericKafkaEventRepository<ShoppingBasket, ShoppingBasketEvent>(ShoppingBasketEvent::class)
