CREATE TABLE reservations
(
    id            BIGSERIAL PRIMARY KEY,
    input_date    TIMESTAMP NOT NULL DEFAULT NOW(),
    start_date    TIMESTAMP NOT NULL,
    end_date      TIMESTAMP NOT NULL,

    room_id       BIGINT    NOT NULL,
    input_user_id BIGINT    NOT NULL,

    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES Room (id),
    CONSTRAINT fk_reservation_user FOREIGN KEY (input_user_id) REFERENCES users (id)
);
