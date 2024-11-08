package org.eda.ecommerce.order.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import java.util.*

abstract class UUIDMergeRepository<T> : PanacheRepositoryBase<T, UUID> {

    fun merge(a: T) {
        entityManager.merge<Any>(a)
    }
}
