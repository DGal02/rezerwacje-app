package pl.wsb.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.wsb.entity.Reservation;
import pl.wsb.entity.Room;
import pl.wsb.service.ReservationService;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Path("/reservations")
@Authenticated
public class ReservationResource {

    @Inject Template reservations_calendar;
    @Inject SecurityIdentity identity;
    @Inject ReservationService reservationService;

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance calendar(@PathParam("roomId") Long roomId,
                                     @QueryParam("error") String error) {
        Room room = Room.findById(roomId);
        if (room == null) throw new NotFoundException();

        return reservations_calendar
                .data("room", room)
                .data("reservations", reservationService.getReservationsByRoom(roomId))
                .data("error", error);
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response add(@FormParam("roomId") Long roomId,
                        @FormParam("startDate") String startDateStr,
                        @FormParam("endDate") String endDateStr) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDateStr);
            LocalDateTime end = LocalDateTime.parse(endDateStr);
            String username = identity.getPrincipal().getName();
            reservationService.addReservation(roomId, username, start, end);

            return Response.seeOther(URI.create("/reservations/" + roomId)).build();

        } catch (IllegalArgumentException e) {
            String message = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return Response.seeOther(URI.create("/reservations/" + roomId + "?error=" + message)).build();
        }
    }

    @POST
    @Path("/delete/{id}")
    public Response delete(@PathParam("id") Long id) {
        var res = Reservation.<Reservation>findById(id);
        if (res == null) return Response.status(Response.Status.NOT_FOUND).build();
        Long roomId = res.room.id;

        try {
            reservationService.deleteReservation(
                    id,
                    identity.getPrincipal().getName(),
                    identity.hasRole("admin")
            );
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.seeOther(URI.create("/reservations/" + roomId)).build();
    }
}
