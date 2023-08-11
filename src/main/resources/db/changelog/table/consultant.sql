--liquibase formatted sql

--changeset shishlov:consultant-1
CREATE TABLE consultant (
    id bigserial not null primary key,
    division varchar(255) not null,
    directing varchar(255) not null,
    service varchar(255) not null,
    subdivision varchar(255),
    full_name varchar(255) not null,
    num_of_tasks int4
);