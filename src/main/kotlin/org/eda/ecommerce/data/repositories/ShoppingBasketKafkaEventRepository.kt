package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import org.eda.ecommerce.data.events.external.incoming.StorableKafkaEvent
import org.eda.ecommerce.data.models.ShoppingBasket
import java.util.UUID

@ApplicationScoped
class ShoppingBasketKafkaEventRepository : PanacheRepositoryBase<StorableKafkaEvent<ShoppingBasket>, UUID>
