package pl.wsb.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.wsb.entity.Room;
import pl.wsb.repository.UserRepository;
import pl.wsb.service.RoomService;

import java.net.URI;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class RoomResource {
    @Inject
    Template rooms;

    @Inject
    Template form_add_room;

    @Inject
    Template form_edit_room;

    @Inject
    RoomService roomService;

    @Inject
    SecurityIdentity identity;

    @Inject
    UserRepository userRepository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getRoomsView() {
        return rooms.data("rooms", roomService.getAllRooms());
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getAddRoomForm() {
        return form_add_room.data("error", null);
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response saveRoom(@FormParam("name") String name,
                             @FormParam("capacity") int capacity,
                             @FormParam("description") String description) {
        try {
            Room newRoom = new Room(name, capacity);
            newRoom.description = description;
            String username = identity.getPrincipal().getName();
            newRoom.inputUser = userRepository.findByUsername(username);
            roomService.addRoom(newRoom);

            return Response.seeOther(URI.create("/rooms")).build();

        } catch (IllegalArgumentException e) {
            return Response.ok(form_add_room.data("error", e.getMessage())).build();
        }
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(Room room) {
        try {
            roomService.addRoom(room);
            return Response.status(201).entity(room).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/delete/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed("admin")
    public Response delete(@PathParam("id") Long id) {
        roomService.deleteRoom(id);
        return Response.seeOther(URI.create("/rooms")).build();
    }

    @GET
    @Path("/edit/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showEditForm(@PathParam("id") Long id) {
        Room room = roomService.getRoom(id);
        if (room == null) {
            return rooms.data("rooms", roomService.getAllRooms());
        }

        return form_edit_room
                .data("room", room)
                .data("error", null);
    }

    @POST
    @Path("/update/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response update(@PathParam("id") Long id,
                           @FormParam("name") String name,
                           @FormParam("capacity") int capacity,
                           @FormParam("description") String description) {

        Room updatedData = new Room(name, capacity);
        updatedData.description = description;
        updatedData.id = id;

        try {
            roomService.updateRoom(id, updatedData);
            return Response.seeOther(URI.create("/rooms")).build();

        } catch (IllegalArgumentException e) {
            return Response.ok(form_edit_room.data("room", updatedData).data("error", e.getMessage())).build();
        }
    }
}
