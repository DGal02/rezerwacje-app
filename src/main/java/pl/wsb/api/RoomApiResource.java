package pl.wsb.api;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import pl.wsb.dto.RoomRequest;
import pl.wsb.dto.RoomResponse;
import pl.wsb.entity.Room;
import pl.wsb.repository.UserRepository;
import pl.wsb.service.RoomService;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Rooms API", description = "Zarządzanie salami (Tylko Admin)")
public class RoomApiResource {
    @Inject RoomService roomService;
    @Inject SecurityIdentity identity;
    @Inject UserRepository userRepository;

    @GET
    @Operation(summary = "Pobierz listę sal", description = "Zwraca wszystkie dostępne sale.")
    public List<RoomResponse> getAll() {
        return roomService.getAllRooms().stream()
                .map(r -> new RoomResponse(
                        r.id,
                        r.name,
                        r.capacity,
                        r.description,
                        r.inputUser != null ? r.inputUser.username : ""
                ))
                .collect(Collectors.toList());
    }

    @POST
    @RolesAllowed("admin")
    @Transactional
    @Operation(summary = "Dodaj nową salę", description = "Wymaga roli ADMIN.")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Utworzono"),
            @APIResponse(responseCode = "403", description = "Brak uprawnień (nie jesteś adminem)"),
            @APIResponse(responseCode = "400", description = "Błąd walidacji (np. nazwa zajęta)")
    })
    public Response create(@RequestBody(required = true) RoomRequest request) {
        try {
            Room room = new Room(request.name(), request.capacity());
            room.description = request.description();
            String username = identity.getPrincipal().getName();
            room.inputUser = userRepository.findByUsername(username);

            roomService.addRoom(room);

            return Response.status(Response.Status.CREATED).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    @Operation(summary = "Usuń salę", description = "Wymaga roli ADMIN.")
    public Response delete(@PathParam("id") Long id) {
        roomService.deleteRoom(id);
        return Response.noContent().build();
    }

    public record ErrorResponse(String error) {}
}