package pl.wsb.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import pl.wsb.entity.Room;

import java.util.List;

@ApplicationScoped
public class RoomService {
    public List<Room> getAllRooms() {
        return Room.listAll();
    }

    @Transactional
    public void addRoom(Room room) {
        if ("Schowek".equalsIgnoreCase(room.name)) {
            throw new IllegalArgumentException("Nie można dodawać schowków jako sal!");
        }

        if (Room.count("name", room.name) > 0) {
            throw new IllegalArgumentException("Sala o nazwie '" + room.name + "' już istnieje!");
        }

        room.persist();
    }
}
