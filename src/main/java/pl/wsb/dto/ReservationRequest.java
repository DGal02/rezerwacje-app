package pl.wsb.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Model danych wymagany do utworzenia rezerwacji")
public record ReservationRequest(

        @Schema(required = true, example = "1", description = "Unikalne ID sali, którą chcesz zarezerwować")
        Long roomId,

        @Schema(required = true, example = "2026-05-20T10:00:00", description = "Data i czas rozpoczęcia (Format ISO-8601)")
        LocalDateTime start,

        @Schema(required = true, example = "2026-05-20T12:00:00", description = "Data i czas zakończenia")
        LocalDateTime end
) {}