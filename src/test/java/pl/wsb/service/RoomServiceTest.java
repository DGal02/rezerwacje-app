package pl.wsb.service;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import jakarta.inject.Inject;
import pl.wsb.entity.Room;

@QuarkusTest
public class RoomServiceTest {
    @Inject
    RoomService roomService;

    @Test
    public void testAddRoomShoudThrowExceptionForSchowek() {
        Room room = new Room();
        room.name = "Schowek";
        room.capacity = 5;

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            roomService.addRoom(room);
        });

        Assertions.assertEquals("Nie można dodawać schowków jako sal!", thrown.getMessage());
    }

    @Test
    public void testAddRoomShouldThrowExceptionIfNameExists() {
        PanacheMock.mock(Room.class);

        Mockito.when(Room.count("name", "Sala Dubel")).thenReturn(1L);

        Room room = new Room();
        room.name = "Sala Dubel";
        room.capacity = 10;

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            roomService.addRoom(room);
        });

        Assertions.assertEquals("Sala o nazwie 'Sala Dubel' już istnieje!", thrown.getMessage());
    }

    @Test
    public void testAddValidRoom() {
        PanacheMock.mock(Room.class);
        Mockito.when(Room.count("name", "Sala Nowa")).thenReturn(0L);

        Room room = new Room();
        room.name = "Sala Nowa";
        room.capacity = 20;

        Assertions.assertDoesNotThrow(() -> {
            roomService.addRoom(room);
        });

        PanacheMock.verify(Room.class, Mockito.times(1)).persist(Mockito.any(Room.class));
    }
}
