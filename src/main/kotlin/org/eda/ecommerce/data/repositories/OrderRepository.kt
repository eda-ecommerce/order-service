package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import org.eda.ecommerce.data.models.Order
import java.util.*

@ApplicationScoped
class OrderRepository : PanacheRepositoryBase<Order, UUID>
