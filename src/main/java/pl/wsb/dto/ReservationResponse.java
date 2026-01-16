package pl.wsb.dto;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        String roomName,
        String reservedBy,
        LocalDateTime start,
        LocalDateTime end,
        LocalDateTime createdAd
) {}