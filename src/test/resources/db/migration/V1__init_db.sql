create type export_type as enum ('doi_list');
create type job_state as enum ('SCHEDULED', 'RUNNING', 'FAILED', 'COMPLETED');

create table export_queue
(
    id             uuid,
    params         jsonb,
    creator        text,
    job_state      job_state,
    time_scheduled timestamp with time zone,
    time_started   timestamp with time zone,
    time_completed timestamp with time zone,
    export_type    export_type,
    hashed_params  uuid
);
