--liquibase formatted sql

--changeset shishlov:event-1
CREATE TABLE event (
    id bigserial not null primary key,
    calendar_id bigint not null references calendar(id),
    dt_start timestamp,
    dt_end timestamp
);

CREATE INDEX calendar_id_idx ON event USING btree (calendar_id);
CREATE INDEX between_idx on event USING btree(dt_start, dt_end)