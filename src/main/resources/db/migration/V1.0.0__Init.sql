CREATE TABLE Room (
                      id BIGINT NOT NULL,
                      capacity INTEGER NOT NULL CHECK (capacity >= 1),
                      description VARCHAR(255),
                      name VARCHAR(255) NOT NULL,
                      PRIMARY KEY (id)
);

ALTER TABLE Room ADD CONSTRAINT unique_room_name UNIQUE (name);

CREATE SEQUENCE Room_SEQ START WITH 1 INCREMENT BY 50;