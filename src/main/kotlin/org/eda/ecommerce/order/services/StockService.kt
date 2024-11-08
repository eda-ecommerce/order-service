package org.eda.ecommerce.order.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eda.ecommerce.order.data.models.ProductQuantity
import org.eda.ecommerce.order.data.models.StockEntry
import org.eda.ecommerce.order.data.repositories.StockRepository
import org.eda.ecommerce.order.exceptions.NoStockRecordsForProductFoundException
import org.eda.ecommerce.order.exceptions.NotEnoughStockException
import java.util.*

@ApplicationScoped
class StockService {

    @Inject
    private lateinit var stockRepository: StockRepository

    @Transactional
    fun adjustStock(productId: UUID, availableStock: Int) {
        println("Adjusting stock for product $productId to $availableStock")

        val stockEntry = stockRepository.findById(productId)

        if (stockEntry == null) {
            println("No stock entry found for product $productId. Creating new entry.")
            val newStockEntry = StockEntry()
            newStockEntry.productId = productId
            newStockEntry.availableStock = availableStock
            stockRepository.persist(newStockEntry)
            return
        }

        stockEntry.availableStock = availableStock
        stockRepository.persist(stockEntry)
    }

    fun validateAllProductsHaveEnoughAvailableQuantity(products: List<ProductQuantity>) {
        products.forEach {
            val stockEntry = stockRepository.findById(it.productId)
                ?: throw NoStockRecordsForProductFoundException(it.productId)

            if (!stockEntry.isAvailableToOrder(it.quantity!!)) {
                throw NotEnoughStockException(it.productId)
            }
        }
    }
}
