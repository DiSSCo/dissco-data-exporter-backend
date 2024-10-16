package eu.dissco.dataexporter.domain;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import java.time.Instant;
import java.util.UUID;

public record ExportJob(
    UUID id,
    JsonNode params,
    String creator,
    JobState jobState,
    Instant timeScheduled,
    Instant timeStarted,
    Instant timeCompleted,
    ExportType exportType,
    UUID hashedParameters,
    String destinationEmail
) {

}
