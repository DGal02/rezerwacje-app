package pl.wsb.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import pl.wsb.entity.User;

@ApplicationScoped
public class UserService {

    @Transactional
    public void registerUser(String username, String plainPassword, String role) {
        if (User.count("username", username) > 0) {
            throw new IllegalArgumentException("Użytkownik o takim loginie już istnieje!");
        }

        User newUser = new User(username, plainPassword, role);
        newUser.persist();
    }
}