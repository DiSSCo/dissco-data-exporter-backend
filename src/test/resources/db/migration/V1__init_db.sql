create type export_type as enum ('doi_list');
create type job_state as enum ('SCHEDULED', 'RUNNING', 'FAILED', 'COMPLETED');

create table export_queue
(
    id                uuid                     not null
        constraint export_queue_pk
            primary key,
    params            jsonb                    not null,
    creator           text                     not null,
    job_state         job_state                not null,
    time_scheduled    timestamp with time zone not null,
    time_started      timestamp with time zone,
    time_completed    timestamp with time zone,
    export_type       export_type              not null,
    hashed_params     uuid                     not null,
    destination_email text                     not null,
    download_link           text
);

