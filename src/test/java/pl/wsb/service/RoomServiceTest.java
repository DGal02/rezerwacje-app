package pl.wsb.service;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import jakarta.inject.Inject;
import pl.wsb.entity.Room;
import pl.wsb.entity.User;

@QuarkusTest
public class RoomServiceTest {

    @Inject
    RoomService roomService;

    private User createDummyUser() {
        User user = new User();
        user.username = "test_admin";
        user.role = "admin";
        return user;
    }

    @Test
    public void testAddRoomShouldThrowExceptionForSchowek() {
        Room room = new Room();
        room.name = "Schowek";
        room.capacity = 5;
        room.inputUser = createDummyUser();
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            roomService.addRoom(room);
        });

        Assertions.assertEquals("Nie można nazywać sal 'Schowek'!", thrown.getMessage());
    }

    @Test
    public void testAddRoomShouldThrowExceptionIfNameExists() {
        PanacheMock.mock(Room.class);
        Mockito.when(Room.count("name", "Sala Dubel")).thenReturn(1L);
        Room room = new Room();
        room.name = "Sala Dubel";
        room.capacity = 10;
        room.inputUser = createDummyUser();
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            roomService.addRoom(room);
        });

        Assertions.assertEquals("Sala o nazwie 'Sala Dubel' już istnieje!", thrown.getMessage());
    }

    @Test
    public void testAddValidRoom() {
        PanacheMock.mock(Room.class);
        Mockito.when(Room.count("name", "Sala Nowa")).thenReturn(0L);
        Room room = Mockito.spy(new Room());
        room.name = "Sala Nowa";
        room.capacity = 20;
        room.inputUser = createDummyUser();
        Mockito.doNothing().when(room).persist();
        Assertions.assertDoesNotThrow(() -> {
            roomService.addRoom(room);
        });

        Mockito.verify(room, Mockito.times(1)).persist();
    }
}