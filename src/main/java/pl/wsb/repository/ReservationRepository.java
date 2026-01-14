package pl.wsb.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import pl.wsb.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReservationRepository implements PanacheRepository<Reservation> {
    public List<Reservation> findUpcomingByRoom(Long roomId) {
        return list("room.id = ?1 AND endDate > ?2 ORDER BY startDate ASC",
                roomId, LocalDateTime.now());
    }
}
