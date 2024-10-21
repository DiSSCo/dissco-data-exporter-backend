package eu.dissco.dataexporter.domain;

import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.schema.SearchParam;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExportJob(
    UUID id,
    List<SearchParam> params,
    String creator,
    JobState jobState,
    Instant timeScheduled,
    Instant timeStarted,
    Instant timeCompleted,
    ExportType exportType,
    UUID hashedParameters,
    String destinationEmail,
    TargetType targetType) {
}
