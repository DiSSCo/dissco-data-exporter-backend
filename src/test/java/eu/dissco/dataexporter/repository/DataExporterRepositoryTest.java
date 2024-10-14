package eu.dissco.dataexporter.repository;

import static eu.dissco.dataexporter.database.jooq.Tables.EXPORT_QUEUE;
import static eu.dissco.dataexporter.utils.TestUtils.ID;
import static eu.dissco.dataexporter.utils.TestUtils.MAPPER;
import static eu.dissco.dataexporter.utils.TestUtils.givenScheduledJob;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import eu.dissco.dataexporter.domain.ExportJob;
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
        .fetchOne(this::recordToJob);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  private ExportJob recordToJob(Record dbRecord) {
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
          dbRecord.get(EXPORT_QUEUE.HASHED_PARAMS)
      );
    } catch (Exception e){
      throw new IllegalStateException();
    }
  }

}