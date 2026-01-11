package pl.wsb.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.net.URI;

@Path("/logout")
public class LogoutResource {
    @Inject
    @ConfigProperty(name = "quarkus.http.auth.form.cookie-name")
    String cookieName;

    @Inject
    @ConfigProperty(name = "rezerwacje.cookie.secure")
    boolean isSecure;

    @GET
    public Response logout() {
        NewCookie killCookie = new NewCookie.Builder(cookieName)
                .path("/")
                .maxAge(0)
                .secure(isSecure)
                .httpOnly(true)
                .build();

        return Response.seeOther(URI.create("/login?logout=true"))
                .cookie(killCookie)
                .build();
    }
}
