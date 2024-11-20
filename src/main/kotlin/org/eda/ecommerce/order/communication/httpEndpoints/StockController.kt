package org.eda.ecommerce.order.communication.httpEndpoints

import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eda.ecommerce.order.data.models.StockEntry
import org.eda.ecommerce.order.services.StockService
import java.util.UUID

@Path("/stock")
class StockController {

    @Inject
    private lateinit var stockService: StockService


    @GET
    @Operation(summary = "Returns a list of all Stocks")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "A list of Stocks.",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Array<StockEntry>::class))]
        )
    )
    fun getAll(): List<StockEntry> {
        return stockService.getAll()
    }

    @GET
    @Path("{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns a Stock by its ID.")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "The Stock with the given ID.",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = StockEntry::class))]
        ),
        APIResponse(responseCode = "404", description = "Stock not found")
    )
    fun getById(
        @PathParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Stock to be returned.",
            schema = Schema(type = SchemaType.STRING, format = "UUID")
        )
        id: UUID
    ): Response {
        val stock = stockService.findById(id)

        return if (stock != null)
            Response.ok(stock).build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }

}
