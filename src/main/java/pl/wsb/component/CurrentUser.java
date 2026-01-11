package pl.wsb.component;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("currentUser")
public class CurrentUser {
    @Inject
    SecurityIdentity identity;

    public String getName() {
        if (identity.isAnonymous()) {
            return null;
        }
        return identity.getPrincipal().getName();
    }

    public boolean isLoggedIn() {
        return !identity.isAnonymous();
    }
}
