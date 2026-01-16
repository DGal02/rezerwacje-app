package pl.wsb.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import pl.wsb.dto.ReservationRequest;
import pl.wsb.dto.ReservationResponse;
import pl.wsb.entity.Reservation;
import pl.wsb.service.ReservationService;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Reservations API", description = "Zarządzanie rezerwacjami przez REST API")
public class ReservationApiResource {

    @Inject
    ReservationService reservationService;

    @Inject
    SecurityIdentity identity;

    @GET
    @Path("/room/{roomId}")
    @Operation(summary = "Pobierz rezerwacje dla sali", description = "Zwraca listę nadchodzących rezerwacji dla podanego ID sali.")
    public List<ReservationResponse> getByRoom(@PathParam("roomId") Long roomId) {
        List<Reservation> entities = reservationService.getReservationsByRoom(roomId);

        return entities.stream()
                .map(r -> new ReservationResponse(
                        r.id,
                        r.room.name,
                        r.inputUser.username,
                        r.startDate,
                        r.endDate,
                        r.inputDate
                ))
                .collect(Collectors.toList());
    }

    @POST
    @Authenticated
    @Operation(
            summary = "Utwórz nową rezerwację",
            description = "Tworzy rezerwację dla zalogowanego użytkownika. Wymaga podania ID sali oraz daty startu i końca."
    )
    @RequestBody(
            description = "Dane nowej rezerwacji (JSON)",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ReservationRequest.class)
            )
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "201",
                    description = "Rezerwacja została pomyślnie utworzona"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Błąd walidacji (np. data końcowa przed początkową, konflikt terminów)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Nie znaleziono sali o podanym ID",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Użytkownik niezalogowany"
            )
    })
    public Response create(ReservationRequest request) {
        try {
            String username = identity.getPrincipal().getName();
            reservationService.addReservation(
                    request.roomId(),
                    username,
                    request.start(),
                    request.end()
            );
            return Response.status(Response.Status.CREATED).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Usuń rezerwację", description = "Tylko admin lub właściciel rezerwacji.")
    public Response delete(@PathParam("id") Long id) {
        try {
            boolean isAdmin = identity.hasRole("admin");
            String username = identity.getPrincipal().getName();

            reservationService.deleteReservation(id, username, isAdmin);

            return Response.noContent().build();

        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(e.getMessage())).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Schema(description = "Standardowa odpowiedź błędu")
    public record ErrorResponse(
            @Schema(example = "W tym terminie sala jest już zajęta!", description = "Opis przyczyny błędu")
            String error
    ) {}}