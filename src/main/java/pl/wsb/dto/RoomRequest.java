package pl.wsb.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Dane wymagane do utworzenia lub edycji sali")
public record RoomRequest(
        @Schema(required = true, example = "Sala Konferencyjna A", description = "Nazwa sali")
        String name,

        @Schema(required = true, example = "50", description = "Maksymalna liczba osób")
        int capacity,

        @Schema(example = "Sala z rzutnikiem i klimatyzacją", description = "Opcjonalny opis wyposażenia")
        String description
) {}