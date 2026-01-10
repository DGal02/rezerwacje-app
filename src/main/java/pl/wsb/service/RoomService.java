package pl.wsb.service;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import pl.wsb.entity.Room;

import java.util.List;

@ApplicationScoped
public class RoomService {

    @Inject
    Logger log;

    public List<Room> getAllRooms() {
        return Room.listAll();
    }

    @Transactional
    public void addRoom(Room room) {
        validateBusinessLogic(room);
        if (Room.count("name", room.name) > 0) {
            throw new IllegalArgumentException("Sala o nazwie '" + room.name + "' już istnieje!");
        }

        room.persist();
        log.infof("Dodano nową salę: %s", room.name);
    }

    @Transactional
    public void deleteRoom(Long id) {
        boolean deleted = Room.deleteById(id);
        if (deleted) {
            log.infof("Usunięto salę o ID: %d", id);
        } else {
            log.warnf("Próba usunięcia nieistniejącej sali o ID: %d", id);
        }
    }

    @Transactional
    public void updateRoom(Long id, Room updatedData) {
        Room entity = this.getRoom(id);
        if (entity == null) {
            throw new IllegalArgumentException("Sala nie istnieje");
        }

        validateBusinessLogic(updatedData);
        long conflicts = Room.count("name = :name and id != :id",
                Parameters.with("name", updatedData.name).and("id", id));

        if (conflicts > 0) {
            throw new IllegalArgumentException("Inna sala o nazwie '" + updatedData.name + "' już istnieje!");
        }

        entity.name = updatedData.name;
        entity.capacity = updatedData.capacity;
        entity.description = updatedData.description;

        log.infof("Zaktualizowano salę ID: %d", id);
    }

    public Room getRoom(Long id) {
        return Room.findById(id);
    }

    private void validateBusinessLogic(Room room) {
        if ("Schowek".equalsIgnoreCase(room.name)) {
            throw new IllegalArgumentException("Nie można nazywać sal 'Schowek'!");
        }
        if (room.capacity < 1) {
            throw new IllegalArgumentException("Pojemność musi być dodatnia!");
        }
    }
}
