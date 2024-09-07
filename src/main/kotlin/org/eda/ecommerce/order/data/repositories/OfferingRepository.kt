package org.eda.ecommerce.order.data.repositories

import jakarta.enterprise.context.ApplicationScoped
import org.eda.ecommerce.order.data.models.Offering

@ApplicationScoped
class OfferingRepository : TestableUUIDEntityRepository<Offering>()
