package eu.dissco.dataexporter.repository;

import static eu.dissco.dataexporter.database.jooq.Tables.EXPORT_QUEUE;
import static eu.dissco.dataexporter.utils.TestUtils.CREATED;
import static eu.dissco.dataexporter.utils.TestUtils.EMAIL;
import static eu.dissco.dataexporter.utils.TestUtils.HASHED_PARAMS;
import static eu.dissco.dataexporter.utils.TestUtils.ID;
import static eu.dissco.dataexporter.utils.TestUtils.MAPPER;
import static eu.dissco.dataexporter.utils.TestUtils.ORCID;
import static eu.dissco.dataexporter.utils.TestUtils.DOWNLOAD_LINK;
import static eu.dissco.dataexporter.utils.TestUtils.givenJobResult;
import static eu.dissco.dataexporter.utils.TestUtils.givenParams;
import static eu.dissco.dataexporter.utils.TestUtils.givenScheduledJob;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.ExportJob;
import org.jooq.JSONB;
import org.jooq.Record;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataExporterRepositoryTest extends BaseRepositoryIT {

  private DataExporterRepository repository;

  @BeforeEach
  void setup() {
    repository = new DataExporterRepository(context, MAPPER);
  }

  @AfterEach
  void destroy() {
    context.truncate(EXPORT_QUEUE).execute();
  }

  @Test
  void testAddJobToQueue() throws Exception {
    // Given
    var expected = givenScheduledJob();

    // When
    repository.addJobToQueue(expected);
    var result = context.select(EXPORT_QUEUE.asterisk())
        .from(EXPORT_QUEUE)
        .where(EXPORT_QUEUE.ID.eq(ID))
        .fetchOne(DataExporterRepositoryTest::recordToJob);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testUpdateJobState() throws Exception {
    // Given
    repository.addJobToQueue(givenScheduledJob());

    // When
    repository.updateJobState(ID, JobState.RUNNING);
    var result = context.select(EXPORT_QUEUE.asterisk())
        .from(EXPORT_QUEUE)
        .where(EXPORT_QUEUE.ID.eq(ID))
        .fetchOne(DataExporterRepositoryTest::recordToJob);

    // Then
    assertThat(result.timeStarted()).isNotNull();
    assertThat(result.jobState()).isEqualTo(JobState.RUNNING);
  }

  @Test
  void testMarkJobAsComplete() throws Exception {
    // Given
    repository.addJobToQueue(givenScheduledJob());
    var jobState = JobState.COMPLETED;

    // When
    repository.markJobAsComplete(givenJobResult(), jobState);
    var result = context.select(EXPORT_QUEUE.asterisk())
        .from(EXPORT_QUEUE)
        .where(EXPORT_QUEUE.ID.eq(ID))
        .fetchOne(DataExporterRepositoryTest::recordToJob);

    // Then
    assertThat(result.timeCompleted()).isNotNull();
    assertThat(result.jobState()).isEqualTo(jobState);
  }

  @Test
  void testGetEmailFromJobId() throws Exception {
    // Given
    repository.addJobToQueue(givenScheduledJob());

    // When
    var result = repository.getUserEmailFromJobId(ID);

    // Then
    assertThat(result).isEqualTo(EMAIL);
  }

  @Test
  void testGetJobResultsIfExists() throws Exception {
    // Given
    context.insertInto(EXPORT_QUEUE)
        .set(EXPORT_QUEUE.ID, ID)
        .set(EXPORT_QUEUE.PARAMS, JSONB.valueOf(MAPPER.writeValueAsString(givenParams())))
        .set(EXPORT_QUEUE.CREATOR, ORCID)
        .set(EXPORT_QUEUE.TIME_SCHEDULED, CREATED)
        .set(EXPORT_QUEUE.EXPORT_TYPE, ExportType.doi_list)
        .set(EXPORT_QUEUE.HASHED_PARAMS, HASHED_PARAMS)
        .set(EXPORT_QUEUE.DESTINATION_EMAIL, EMAIL)
        .set(EXPORT_QUEUE.JOB_STATE, JobState.COMPLETED)
        .set(EXPORT_QUEUE.DOWNLOAD_LINK, DOWNLOAD_LINK)
        .execute();

    // When
    var result = repository.getJobResultsIfExists(HASHED_PARAMS);

    // Then
    assertThat(result).contains(DOWNLOAD_LINK);
  }


  private static ExportJob recordToJob(Record dbRecord) {
    try {
      return new ExportJob(
          dbRecord.get(EXPORT_QUEUE.ID),
          MAPPER.readValue(dbRecord.get(EXPORT_QUEUE.PARAMS).data(), JsonNode.class),
          dbRecord.get(EXPORT_QUEUE.CREATOR),
          dbRecord.get(EXPORT_QUEUE.JOB_STATE),
          dbRecord.get(EXPORT_QUEUE.TIME_SCHEDULED),
          dbRecord.get(EXPORT_QUEUE.TIME_STARTED),
          dbRecord.get(EXPORT_QUEUE.TIME_COMPLETED),
          dbRecord.get(EXPORT_QUEUE.EXPORT_TYPE),
          dbRecord.get(EXPORT_QUEUE.HASHED_PARAMS),
          dbRecord.get(EXPORT_QUEUE.DESTINATION_EMAIL)
      );
    } catch (Exception e){
      throw new IllegalStateException();
    }
  }

}
