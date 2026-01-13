ALTER TABLE Room
    ADD COLUMN input_user_id BIGINT NOT NULL;

ALTER TABLE Room
    ADD CONSTRAINT fk_room_input_user
        FOREIGN KEY (input_user_id)
            REFERENCES users(id);