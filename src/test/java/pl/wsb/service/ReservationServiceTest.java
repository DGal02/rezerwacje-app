package pl.wsb.service;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.wsb.entity.Reservation;
import pl.wsb.entity.Room;
import pl.wsb.entity.User;
import pl.wsb.repository.ReservationRepository;
import pl.wsb.repository.UserRepository;

import java.time.LocalDateTime;

@QuarkusTest
public class ReservationServiceTest {

    @Inject
    ReservationService reservationService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    ReservationRepository reservationRepository;

    private Room dummyRoom;
    private User dummyUser;

    @BeforeEach
    public void setup() {
        PanacheMock.mock(Reservation.class);
        PanacheMock.mock(Room.class);

        dummyRoom = new Room();
        dummyRoom.id = 1L;
        dummyRoom.name = "Sala Testowa";

        dummyUser = new User();
        dummyUser.username = "tester";
        dummyUser.id = 1L;

        Mockito.when(userRepository.findByUsername("tester")).thenReturn(dummyUser);
    }

    @Test
    public void testAddReservationSuccess() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).plusHours(10);
        LocalDateTime end = LocalDateTime.now().plusDays(1).plusHours(12);

        Mockito.when(Reservation.count(
                Mockito.anyString(),
                Mockito.any(Object[].class))
        ).thenReturn(0L);

        Mockito.when(Room.findById(1L)).thenReturn(dummyRoom);

        Assertions.assertDoesNotThrow(() -> {
            reservationService.addReservation(1L, "tester", start, end);
        });

        Mockito.verify(reservationRepository, Mockito.times(1))
                .persist(Mockito.any(Reservation.class));
    }

    @Test
    public void testAddReservationUserNotFound() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1).plusHours(1);

        Mockito.when(Reservation.count(Mockito.anyString(), Mockito.any(Object[].class))).thenReturn(0L);
        Mockito.when(Room.findById(1L)).thenReturn(dummyRoom);
        Mockito.when(userRepository.findByUsername("nieznany")).thenReturn(null);

        Assertions.assertThrows(NotFoundException.class, () -> {
            reservationService.addReservation(1L, "nieznany", start, end);
        });
    }

    @Test
    public void testAddReservationOverlapDetected() {
        LocalDateTime start = LocalDateTime.now().plusHours(10);
        LocalDateTime end = LocalDateTime.now().plusHours(12);

        Mockito.when(Reservation.count(Mockito.anyString(), Mockito.any(Object[].class))).thenReturn(1L);

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            reservationService.addReservation(1L, "tester", start, end);
        });

        Assertions.assertEquals("W tym terminie sala jest już zajęta!", thrown.getMessage());
    }

    @Test
    public void testAddReservationDatesInvalid() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            reservationService.addReservation(1L, "tester", start, end);
        });
        Assertions.assertEquals("Data końcowa musi być późniejsza niż początkowa!", thrown.getMessage());
    }

    @Test
    public void testDeleteReservationForbidden() {
        Reservation res = new Reservation();
        res.inputUser = dummyUser;
        Mockito.when(Reservation.findById(99L)).thenReturn(res);

        Assertions.assertThrows(ForbiddenException.class, () -> {
            reservationService.deleteReservation(99L, "hacker", false);
        });
    }

    @Test
    public void testDeleteReservationAsOwner() {
        Reservation res = Mockito.spy(new Reservation());
        res.inputUser = dummyUser;
        res.room = dummyRoom;

        Mockito.when(Reservation.findById(99L)).thenReturn(res);
        Mockito.doNothing().when(res).delete();

        Assertions.assertDoesNotThrow(() -> {
            reservationService.deleteReservation(99L, "tester", false);
        });
        Mockito.verify(res).delete();
    }

    @Test
    public void testDeleteReservationAsAdmin() {
        Reservation res = Mockito.spy(new Reservation());
        res.inputUser = dummyUser;

        Mockito.when(Reservation.findById(99L)).thenReturn(res);
        Mockito.doNothing().when(res).delete();

        Assertions.assertDoesNotThrow(() -> {
            reservationService.deleteReservation(99L, "admin", true);
        });
        Mockito.verify(res).delete();
    }
}