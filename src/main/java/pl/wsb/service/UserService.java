package pl.wsb.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;
import pl.wsb.entity.User;
import pl.wsb.repository.UserRepository;

@ApplicationScoped
public class UserService {
    private static final Logger LOG = Logger.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    @Transactional
    public void registerUser(String username, String plainPassword, String role) {
        if (User.count("username", username) > 0) {
            throw new IllegalArgumentException("Użytkownik o takim loginie już istnieje!");
        }

        User newUser = new User(username, plainPassword, role);
        newUser.persist();
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("Użytkownik nie istnieje");
        }

        if (!BcryptUtil.matches(oldPassword, user.password)) {
            LOG.warnv("Nieudana próba zmiany hasła dla {0} - błędne stare hasło", username);
            throw new BadRequestException("Stare hasło jest nieprawidłowe!");
        }
        user.changePassword(newPassword);

        LOG.infov("Hasło użytkownika {0} zostało zmienione", username);
    }
}