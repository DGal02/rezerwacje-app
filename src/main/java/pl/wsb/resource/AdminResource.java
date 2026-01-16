package pl.wsb.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.wsb.service.UserService;

import java.net.URI;

@Path("/admin")
@RolesAllowed("admin")
public class AdminResource {

    @Inject
    UserService userService;

    @Inject
    Template admin_user_add;

    @GET
    @Path("/users/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getAddUserForm() {
        return admin_user_add.data("error", null);
    }

    @POST
    @Path("/users/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response saveUser(@FormParam("username") String username,
                             @FormParam("password") String password,
                             @FormParam("role") String role) {
        try {
            userService.registerUser(username, password, role);
            return Response.seeOther(URI.create("/rooms")).build();

        } catch (IllegalArgumentException e) {
            return Response.ok(admin_user_add.data("error", e.getMessage())).build();
        }
    }
}