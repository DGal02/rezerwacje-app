package pl.wsb.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.wsb.entity.Reservation;
import pl.wsb.entity.Room;
import pl.wsb.entity.User;
import pl.wsb.repository.UserRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Path("/reservations")
@Authenticated
public class ReservationResource {

    @Inject
    Template reservations_calendar;

    @Inject
    SecurityIdentity identity;

    @Inject
    UserRepository userRepository;

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance calendar(@PathParam("roomId") Long roomId) {
        Room room = Room.findById(roomId);
        if (room == null) {
            throw new NotFoundException("Sala nie istnieje");
        }

        List<Reservation> roomReservations = Reservation.listByRoom(roomId);

        return reservations_calendar
                .data("room", room)
                .data("reservations", roomReservations);
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response add(@FormParam("roomId") Long roomId,
                        @FormParam("startDate") String startDateStr,
                        @FormParam("endDate") String endDateStr) {

        LocalDateTime start = LocalDateTime.parse(startDateStr);
        LocalDateTime end = LocalDateTime.parse(endDateStr);

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Data końcowa nie może być przed początkową!");
        }

        Room room = Room.findById(roomId);
        User user = userRepository.findByUsername(identity.getPrincipal().getName());

        Reservation res = new Reservation();
        res.room = room;
        res.inputUser = user;
        res.inputDate = LocalDateTime.now();
        res.startDate = start;
        res.endDate = end;

        res.persist();

        return Response.seeOther(URI.create("/reservations/" + roomId)).build();
    }

    @POST
    @Path("/delete/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Reservation res = Reservation.findById(id);
        if (res == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String currentUser = identity.getPrincipal().getName();
        boolean isAdmin = identity.hasRole("admin");
        boolean isOwner = res.inputUser.username.equals(currentUser);

        if (!isAdmin && !isOwner) {
            return Response.status(Response.Status.FORBIDDEN).entity("Brak uprawnień do usunięcia tej rezerwacji").build();
        }

        Long roomId = res.room.id;

        res.delete();

        return Response.seeOther(URI.create("/reservations/" + roomId)).build();
    }
}