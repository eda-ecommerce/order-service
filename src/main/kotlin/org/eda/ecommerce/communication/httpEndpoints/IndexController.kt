package org.eda.ecommerce.communication.httpEndpoints

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriBuilder
import org.eclipse.microprofile.openapi.annotations.Operation


@ApplicationScoped
@Path("/")
class IndexController {

    @GET
    @Operation(summary = "Redirects to the Swagger UI")
    fun redirectToSwagger(): Response {
        val location = UriBuilder.fromPath("/q/swagger-ui").build()
        return Response.seeOther(location).build()
    }
}
