package org.eda.ecommerce.data.events.external.incoming

import jakarta.persistence.*
import org.eda.ecommerce.data.models.ShoppingBasket

@Entity
class ShoppingBasketEvent : StorableKafkaEvent<ShoppingBasket>()
