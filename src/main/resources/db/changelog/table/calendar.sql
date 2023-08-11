--liquibase formatted sql

--changeset shishlov:calendar-1
CREATE TABLE calendar (
        id bigserial not null primary key
);