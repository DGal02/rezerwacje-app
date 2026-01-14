package pl.wsb.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reservations")
public class Reservation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "input_date", nullable = false)
    public LocalDateTime inputDate;

    @Column(name = "start_date", nullable = false)
    public LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    public LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    public Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_user_id", nullable = false)
    public User inputUser;
}
