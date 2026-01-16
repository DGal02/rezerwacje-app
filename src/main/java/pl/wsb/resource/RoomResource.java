package pl.wsb.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import pl.wsb.entity.Room;
import pl.wsb.repository.UserRepository;
import pl.wsb.service.RoomService;

import java.net.URI;

@Path("/rooms")
@Authenticated
public class RoomResource {

    @Inject Template rooms;
    @Inject Template form_add_room;
    @Inject Template form_edit_room;
    @Inject RoomService roomService;
    @Inject SecurityIdentity identity;
    @Inject UserRepository userRepository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(hidden = true)
    public TemplateInstance getAllRoomsHtml() {
        return rooms.data("rooms", roomService.getAllRooms());
    }

    @GET
    @Path("/new")
    @RolesAllowed("admin")
    @Produces(MediaType.TEXT_HTML)
    @Operation(hidden = true)
    public TemplateInstance getAddRoomForm() {
        return form_add_room.data("error", null);
    }

    @POST
    @Path("/save")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(hidden = true)
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

    @GET
    @Path("/edit/{id}")
    @RolesAllowed("admin")
    @Produces(MediaType.TEXT_HTML)
    @Operation(hidden = true)
    public TemplateInstance showEditForm(@PathParam("id") Long id) {
        Room room = roomService.getRoom(id);
        if (room == null) return rooms.data("rooms", roomService.getAllRooms());

        return form_edit_room.data("room", room).data("error", null);
    }

    @POST
    @Path("/update/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(hidden = true)
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

    @POST
    @Path("/delete/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(hidden = true)
    public Response delete(@PathParam("id") Long id) {
        roomService.deleteRoom(id);
        return Response.seeOther(URI.create("/rooms")).build();
    }
}