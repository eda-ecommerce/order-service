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
import org.eda.ecommerce.order.data.models.Order
import org.eda.ecommerce.order.exceptions.OrderCancelledException
import org.eda.ecommerce.order.exceptions.OrderNotFoundException
import org.eda.ecommerce.order.services.OrderService
import java.net.URI
import java.util.UUID

@Path("/order")
class OrderController {

    @Inject
    private lateinit var orderService: OrderService


    @GET
    @Operation(summary = "Returns a list of all Orders")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "A list of Orders.",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Array<Order>::class))]
        )
    )
    fun getAll(): List<Order> {
        return orderService.getAll()
    }

    @GET
    @Path("{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns a Order by its ID.")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "The Order with the given ID.",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Order::class))]
        ),
        APIResponse(responseCode = "404", description = "Order not found")
    )
    fun getById(
        @PathParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Order to be returned.",
            schema = Schema(type = SchemaType.STRING, format = "UUID")
        )
        id: UUID
    ): Response {
        val order = orderService.findById(id)

        return if (order != null)
            Response.ok(order).build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }

    @POST
    @Path("{id}/confirm")
    @Operation(summary = "Confirm an Order.")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Order created",
        ),
        APIResponse(
            responseCode = "406",
            description = "Order could not be confirmed because it was canceled",
        ),
        APIResponse(
            responseCode = "404",
            description = "Order not found and thus could not be confirmed",
        )
    )
    fun confirm(
        @PathParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Order to be confirmed.",
            schema = Schema(type = SchemaType.STRING, format = "UUID")
        )
        id: UUID
    ): Response {
        try {
            orderService.confirmOrder(id)
        } catch (e: OrderNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).entity(e).build()
        } catch (e: OrderCancelledException) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(e).build()
        }

        return Response.ok(URI.create("/order/$id")).build()
    }

    @POST
    @Path("{id}/cancel")
    @Operation(summary = "Cancel an Order.")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Order canceled",
        ),
        APIResponse(
            responseCode = "404",
            description = "Order could not be canceled",
        )
    )
    fun cancel(
        @PathParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Order to be confirmed.",
            schema = Schema(type = SchemaType.STRING, format = "UUID")
        )
        id: UUID
    ): Response {
        try {
            orderService.cancelOrder(id)
        } catch (e: OrderNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).entity(e).build()
        }

        return Response.ok(URI.create("/order/$id")).build()
    }
}
