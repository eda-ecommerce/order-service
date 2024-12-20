package org.eda.ecommerce.order.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eda.ecommerce.order.data.models.Offering
import org.eda.ecommerce.order.data.repositories.OfferingRepository
import org.eda.ecommerce.order.exceptions.OfferingNotActiveException
import org.eda.ecommerce.order.exceptions.OfferingNotFoundException
import java.util.*

@ApplicationScoped
class OfferingService {

    @Inject
    private lateinit var offeringRepository: OfferingRepository

    fun getAll(): List<Offering> {
        return offeringRepository.listAll()
    }

    fun findById(id: UUID): Offering? {
        return offeringRepository.findById(id)
    }

    fun saveOffering(offering: Offering) {
        println("Saved Offering: $offering")
        offeringRepository.persist(offering)
    }

    fun updateOffering(offering: Offering) {
        val entity = offeringRepository.findById(offering.id) ?: return

        entity.apply {
            quantity = offering.quantity
            productId = offering.productId
            status = offering.status
        }

        println("Updated Offering: $entity")
        offeringRepository.persist(entity)
    }

    fun getOfferingIfAvailableForOrder(id: UUID): Offering {
        val offering = findById(id) ?: throw OfferingNotFoundException(id)

        if (!offering.isAvailableToOrder()) {
            throw OfferingNotActiveException(id)
        }

        return offering
    }
}
