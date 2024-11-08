package org.eda.ecommerce.order.data.repositories

import jakarta.enterprise.context.control.ActivateRequestContext
import java.util.*

abstract class TestableUUIDEntityRepository<T> : UUIDMergeRepository<T>() {

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
