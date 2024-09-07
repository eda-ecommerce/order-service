package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.control.ActivateRequestContext
import java.util.*

abstract class TestableUUIDEntityRepository<T> : PanacheRepositoryBase<T, UUID> {
    @ActivateRequestContext
    fun countWithRequestContext(): Long {
        return count()
    }

    @ActivateRequestContext
    fun getFirstWithRequestContext(): T {
        return listAll().first()
    }

    @ActivateRequestContext
    fun findByIdWithRequestContext(id: UUID): T? {
        return findById(id)
    }
}
