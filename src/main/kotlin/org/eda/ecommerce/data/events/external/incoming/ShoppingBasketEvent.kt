package org.eda.ecommerce.data.events.external.incoming

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.eda.ecommerce.data.models.ShoppingBasket
import org.hibernate.annotations.GenericGenerator
import java.util.*

@Entity
class ShoppingBasketEvent : StorableKafkaEvent<ShoppingBasket>() {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    override lateinit var id: UUID
}
