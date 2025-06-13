package eu.dissco.dataexporter.repository;

import static eu.dissco.dataexporter.database.jooq.Tables.EXPORT_QUEUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.ExportJob;
import eu.dissco.dataexporter.domain.JobResult;
import eu.dissco.dataexporter.domain.TargetType;
import eu.dissco.dataexporter.exception.DatabaseRuntimeException;
import eu.dissco.dataexporter.exception.InvalidRequestException;
import eu.dissco.dataexporter.schema.SearchParam;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DataExporterRepository {

  private final DSLContext context;
  private final ObjectMapper mapper;

  public void addJobToQueue(ExportJob job) throws InvalidRequestException {
    context.insertInto(EXPORT_QUEUE)
        .set(EXPORT_QUEUE.ID, job.id())
        .set(EXPORT_QUEUE.PARAMS, mapToJSONB(job.params()))
        .set(EXPORT_QUEUE.JOB_STATE, job.jobState())
        .set(EXPORT_QUEUE.CREATOR, job.creator())
        .set(EXPORT_QUEUE.TIME_SCHEDULED, job.timeScheduled())
        .set(EXPORT_QUEUE.EXPORT_TYPE, job.exportType())
        .set(EXPORT_QUEUE.HASHED_PARAMS, job.hashedParameters())
        .set(EXPORT_QUEUE.DESTINATION_EMAIL, job.destinationEmail())
        .set(EXPORT_QUEUE.IS_SOURCE_SYSTEM_JOB, job.isSourceSystemJob())
        .set(EXPORT_QUEUE.TARGET_TYPE, job.targetType().getName())
        .execute();
  }

  public Integer getRunningJobs() {
    return context.selectCount()
        .from(EXPORT_QUEUE)
        .where(EXPORT_QUEUE.JOB_STATE.eq(JobState.RUNNING))
        .fetchOne(0, Integer.class);
  }

  public void updateJobState(UUID id, JobState jobState) {
    context.update(EXPORT_QUEUE)
        .set(EXPORT_QUEUE.JOB_STATE, jobState)
        .set(EXPORT_QUEUE.TIME_STARTED, Instant.now())
        .where(EXPORT_QUEUE.ID.eq(id))
        .execute();
  }

  public void markJobAsComplete(JobResult jobResult, JobState jobState) {
    context.update(EXPORT_QUEUE)
        .set(EXPORT_QUEUE.JOB_STATE, jobState)
        .set(EXPORT_QUEUE.TIME_COMPLETED, Instant.now())
        .set(EXPORT_QUEUE.DOWNLOAD_LINK, jobResult.downloadLink())
        .where(EXPORT_QUEUE.ID.eq(jobResult.id()))
        .execute();
  }

  public ExportJob getExportJob(UUID id) {
    return context.select(EXPORT_QUEUE.asterisk())
        .from(EXPORT_QUEUE)
        .where(EXPORT_QUEUE.ID.eq(id))
        .fetchOne(this::recordToExportJob);
  }

  public Optional<ExportJob> getNextJobInQueue() {
    var result = context.select(EXPORT_QUEUE.asterisk())
        .from(EXPORT_QUEUE)
        .where(EXPORT_QUEUE.JOB_STATE.eq(JobState.SCHEDULED))
        .orderBy(EXPORT_QUEUE.TIME_SCHEDULED.asc())
        .limit(1)
        .fetchOne(this::recordToExportJob);
    if (result == null) {
      return Optional.empty();
    } else {
      return Optional.of(result);
    }
  }

  private JSONB mapToJSONB(List<SearchParam> searchParams) throws InvalidRequestException {
    try {
      return JSONB.valueOf(mapper.writeValueAsString(searchParams));
    } catch (JsonProcessingException e) {
      log.error("Unable to parse params to JSONB", e);
      throw new InvalidRequestException("Unable to parse params");
    }
  }

  private ExportJob recordToExportJob(Record dbRecord) {
    var jobId = dbRecord.get(EXPORT_QUEUE.ID);
    try {
      return new ExportJob(
          jobId,
          mapper.readValue(dbRecord.get(EXPORT_QUEUE.PARAMS).data(),
              new TypeReference<List<SearchParam>>() {
              }),
          dbRecord.get(EXPORT_QUEUE.CREATOR),
          dbRecord.get(EXPORT_QUEUE.JOB_STATE),
          dbRecord.get(EXPORT_QUEUE.TIME_SCHEDULED),
          dbRecord.get(EXPORT_QUEUE.TIME_STARTED),
          dbRecord.get(EXPORT_QUEUE.TIME_COMPLETED),
          dbRecord.get(EXPORT_QUEUE.EXPORT_TYPE),
          dbRecord.get(EXPORT_QUEUE.HASHED_PARAMS),
          dbRecord.get(EXPORT_QUEUE.DESTINATION_EMAIL),
          TargetType.fromString(dbRecord.get(EXPORT_QUEUE.TARGET_TYPE)),
          dbRecord.get(EXPORT_QUEUE.IS_SOURCE_SYSTEM_JOB),
          dbRecord.get(EXPORT_QUEUE.DOWNLOAD_LINK));
    } catch (IllegalArgumentException | JsonProcessingException e) {
      log.error("Unable to read latest record with id {} from database", jobId, e);
      updateJobState(jobId, JobState.FAILED);
      throw new DatabaseRuntimeException("Unable to read record from database");
    }
  }

}
