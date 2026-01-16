package pl.wsb.dto;

public record RoomResponse(
        Long id,
        String name,
        int capacity,
        String description,
        String ownerName
) {}