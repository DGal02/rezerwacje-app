package pl.wsb.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Room extends PanacheEntity {
    @NotBlank(message = "Nazwa sali jest wymagana")
    @Column(unique = true)
    public String name;

    @Min(value = 1, message = "Pojemność musi być większa od 0")
    public int capacity;

    public String description;

    public Room() {}

    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }
}
