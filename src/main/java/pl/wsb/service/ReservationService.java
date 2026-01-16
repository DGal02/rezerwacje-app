package pl.wsb.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;
import pl.wsb.entity.Reservation;
import pl.wsb.entity.Room;
import pl.wsb.entity.User;
import pl.wsb.repository.ReservationRepository;
import pl.wsb.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReservationService {
    private static final Logger LOG = Logger.getLogger(ReservationService.class);

    @Inject
    UserRepository userRepository;

    @Inject
    ReservationRepository reservationRepository;

    public List<Reservation> getReservationsByRoom(Long roomId) {
        LOG.debugv("Pobieranie rezerwacji dla sali ID: {0}", roomId);
        return reservationRepository.findUpcomingByRoom(roomId);
    }

    @Transactional
    public void addReservation(Long roomId, String username, LocalDateTime start, LocalDateTime end) {
        LOG.infov("Próba dodania rezerwacji: sala={0}, user={1}, start={2}, end={3}",
                roomId, username, start, end);

        if (end.isBefore(start) || end.equals(start)) {
            LOG.warn("Błąd walidacji: Data końcowa jest przed początkową.");
            throw new IllegalArgumentException("Data końcowa musi być późniejsza niż początkowa!");
        }

        if (start.isBefore(LocalDateTime.now())) {
            LOG.warn("Błąd walidacji: Próba rezerwacji w przeszłości.");
            throw new IllegalArgumentException("Nie można rezerwować w przeszłości!");
        }

        long overlaps = Reservation.count(
                "room.id = ?1 AND startDate < ?2 AND endDate > ?3",
                roomId, end, start
        );

        if (overlaps > 0) {
            LOG.warnv("Konflikt terminów dla sali {0} w podanym czasie.", roomId);
            throw new IllegalArgumentException("W tym terminie sala jest już zajęta!");
        }

        Room room = Room.findById(roomId);
        if (room == null) {
            LOG.errorv("Błąd spójności: Nie znaleziono sali o ID {0}", roomId);
            throw new NotFoundException("Sala nie istnieje");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            LOG.errorv("Błąd spójności: Nie znaleziono użytkownika {0}", username);
            throw new NotFoundException("Użytkownik nie istnieje");
        }

        Reservation res = new Reservation();
        res.room = room;
        res.inputUser = user;
        res.inputDate = LocalDateTime.now();
        res.startDate = start;
        res.endDate = end;
        reservationRepository.persist(res);

        LOG.infov("Rezerwacja została pomyślnie utworzona. ID (po flush): {0}", res.id);
    }

    @Transactional
    public void deleteReservation(Long reservationId, String currentUsername, boolean isAdmin) {
        LOG.infov("Żądanie usunięcia rezerwacji ID: {0} przez użytkownika: {1} (Admin: {2})",
                reservationId, currentUsername, isAdmin);

        Reservation res = Reservation.findById(reservationId);
        if (res == null) {
            LOG.warnv("Próba usunięcia nieistniejącej rezerwacji ID: {0}", reservationId);
            throw new NotFoundException("Rezerwacja nie istnieje");
        }

        boolean isOwner = res.inputUser.username.equals(currentUsername);

        if (!isAdmin && !isOwner) {
            LOG.warnv("SECURITY: Odmowa dostępu. Użytkownik {0} próbował usunąć cudzą rezerwację {1}",
                    currentUsername, reservationId);
            throw new ForbiddenException("Brak uprawnień do usunięcia tej rezerwacji");
        }

        res.delete();
        LOG.info("Rezerwacja została usunięta pomyślnie.");
    }
}