package pl.wsb.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import pl.wsb.entity.Reservation;
import pl.wsb.entity.Room;
import pl.wsb.entity.User;
import pl.wsb.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReservationService {
    @Inject
    UserRepository userRepository;

    public List<Reservation> getReservationsByRoom(Long roomId) {
        return Reservation.listByRoom(roomId);
    }

    @Transactional
    public void addReservation(Long roomId, String username, LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start) || end.equals(start)) {
            throw new IllegalArgumentException("Data końcowa musi być późniejsza niż początkowa!");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Nie można rezerwować w przeszłości!");
        }

        long overlaps = Reservation.count(
                "room.id = ?1 AND startDate < ?2 AND endDate > ?3",
                roomId, end, start
        );

        if (overlaps > 0) {
            throw new IllegalArgumentException("W tym terminie sala jest już zajęta!");
        }

        Room room = Room.findById(roomId);
        if (room == null) {
            throw new NotFoundException("Sala nie istnieje");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("Użytkownik nie istnieje");
        }

        Reservation res = new Reservation();
        res.room = room;
        res.inputUser = user;
        res.inputDate = LocalDateTime.now();
        res.startDate = start;
        res.endDate = end;

        res.persist();
    }

    @Transactional
    public void deleteReservation(Long reservationId, String currentUsername, boolean isAdmin) {
        Reservation res = Reservation.findById(reservationId);
        if (res == null) {
            throw new NotFoundException("Rezerwacja nie istnieje");
        }

        boolean isOwner = res.inputUser.username.equals(currentUsername);
        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Brak uprawnień do usunięcia tej rezerwacji");
        }

        res.delete();
    }
}