create type export_type as enum ('DOI_LIST');
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
    download_link     text,
    target_type       text                     not null,
    is_source_system_job boolean
);

create type translator_type as enum ('biocase', 'dwca');

create table source_system
(
    id text not null
        primary key,
    version integer default 1 not null,
    name text not null,
    endpoint text not null,
    created timestamp with time zone not null,
    modified timestamp with time zone not null,
    tombstoned timestamp with time zone,
    mapping_id text not null,
    creator text not null,
    translator_type translator_type not null,
    data jsonb not null,
    dwc_dp_link text,
    dwca_link text,
    eml bytea
);
