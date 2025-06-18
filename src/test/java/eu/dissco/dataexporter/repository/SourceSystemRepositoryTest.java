package eu.dissco.dataexporter.repository;

import static eu.dissco.dataexporter.database.jooq.Tables.SOURCE_SYSTEM;
import static eu.dissco.dataexporter.utils.TestUtils.SOURCE_SYSTEM_ID;
import static org.assertj.core.api.Assertions.assertThat;

import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.database.jooq.enums.TranslatorType;
import java.time.Instant;
import org.jooq.JSONB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SourceSystemRepositoryTest extends BaseRepositoryIT {

  private SourceSystemRepository repository;

  @BeforeEach
  void setup() {
    repository = new SourceSystemRepository(context);
  }

  @AfterEach
  void destroy() {
    context.truncate(SOURCE_SYSTEM).execute();
  }

  @Test
  void testAddDownloadLinkToJob() {
    // Given
    var downloadLink = "http://example.com/download.zip";
    var shortSourceSystemId = "TEST/CPX-0AF-717";
    insertTestRecord(shortSourceSystemId);

    // When
    var result = repository.addDownloadLinkToJob(ExportType.DWC_DP, SOURCE_SYSTEM_ID, downloadLink);

    // Then
    assertThat(result).isEqualTo(JobState.COMPLETED);
    var storedLink = context.select(SOURCE_SYSTEM.DWC_DP_LINK).from(SOURCE_SYSTEM)
        .where(SOURCE_SYSTEM.ID.eq(shortSourceSystemId)).fetchOne().value1();
    assertThat(storedLink).isEqualTo(downloadLink);
  }

  @Test
  void testAddDownloadLinkToJobNoResult() {
    // Given
    var downloadLink = "http://example.com/download.zip";

    // When
    var result = repository.addDownloadLinkToJob(ExportType.DWC_DP, SOURCE_SYSTEM_ID, downloadLink);

    // Then
    assertThat(result).isEqualTo(JobState.FAILED);
  }

  private void insertTestRecord(String sourceSystemId) {
    context.insertInto(SOURCE_SYSTEM)
        .set(SOURCE_SYSTEM.ID, sourceSystemId)
        .set(SOURCE_SYSTEM.NAME, "Naturalis Vermes Dataset")
        .set(SOURCE_SYSTEM.ENDPOINT, "https://example.com/endpoint")
        .set(SOURCE_SYSTEM.CREATOR, "e2befba6-9324-4bb4-9f41-d7dfae4a44b0")
        .set(SOURCE_SYSTEM.CREATED, Instant.parse("2022-09-16T08:25:01.00Z"))
        .set(SOURCE_SYSTEM.MODIFIED, Instant.parse("2022-09-16T08:25:01.00Z"))
        .set(SOURCE_SYSTEM.TRANSLATOR_TYPE, TranslatorType.dwca)
        .set(SOURCE_SYSTEM.MAPPING_ID, "20.5000.1025/GW0-POP-XAS")
        .set(SOURCE_SYSTEM.DATA, JSONB.valueOf("{}"))
        .execute();
  }

}
