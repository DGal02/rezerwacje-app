package pl.wsb.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import pl.wsb.service.UserService;

@Path("/profile")
@Authenticated
public class UserPanelResource {

    @Inject
    Template user_password_change;

    @Inject
    UserService userService;

    @Inject
    SecurityIdentity identity;

    @GET
    @Path("/password")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getChangePasswordForm() {
        return user_password_change.data("error", null).data("success", null);
    }

    @POST
    @Path("/password/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance changePassword(@FormParam("oldPassword") String oldPassword,
                                           @FormParam("newPassword") String newPassword) {

        String username = identity.getPrincipal().getName();

        try {
            userService.changePassword(username, oldPassword, newPassword);
            return user_password_change
                    .data("error", null)
                    .data("success", "Hasło zostało pomyślnie zmienione!");

        } catch (Exception e) {
            return user_password_change
                    .data("error", e.getMessage())
                    .data("success", null);
        }
    }
}