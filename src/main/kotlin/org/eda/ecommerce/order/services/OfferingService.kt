package org.eda.ecommerce.order.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eda.ecommerce.order.data.models.Offering
import org.eda.ecommerce.order.data.repositories.OfferingRepository
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

    fun createNewOffering(offering: Offering) {
        println("Creating Offering: $offering")
        offeringRepository.persist(offering)
    }

    fun updateOffering(offering: Offering) {
        val entity = offeringRepository.findById(offering.id) ?: return

        entity.apply {
            quantity = offering.quantity
            productId = offering.productId
        }
    }
}
