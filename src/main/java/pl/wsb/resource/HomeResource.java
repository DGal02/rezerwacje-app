package pl.wsb.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class HomeResource {

    @GET
    public Response redirectToRooms() {
        return Response.seeOther(URI.create("/rooms")).build();
    }
}