package org.eda.ecommerce.data.repositories

import jakarta.enterprise.context.ApplicationScoped
import org.eda.ecommerce.data.models.Order

@ApplicationScoped
class OrderRepository : TestableUUIDEntityRepository<Order>()
