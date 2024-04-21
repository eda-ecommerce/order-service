package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eda.ecommerce.data.models.Order
import java.util.*

@ApplicationScoped
class OrderRepository : PanacheRepositoryBase<Order, UUID> {
    @ActivateRequestContext
    fun countWithRequestContext() : Long {
        return count()
    }

    @ActivateRequestContext
    fun getFirstWithRequestContext() : Order {
        return listAll().first()
    }

    @ActivateRequestContext
    fun findByIdWithRequestContext(id: UUID): Order? {
        return findById(id)
    }
}
