package eu.dissco.dataexporter.repository;

import eu.dissco.dataexporter.database.jooq.Tables;
import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.database.jooq.tables.records.SourceSystemRecord;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.TableField;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SourceSystemRepository {

  private static final String HANDLE_PROXY = "https://hdl.handle.net/";
  private static final Map<ExportType, TableField<SourceSystemRecord, String>> downloadLinkColumns = Map.of(
      ExportType.DWC_DP, Tables.SOURCE_SYSTEM.DWC_DP_LINK,
      ExportType.DWCA, Tables.SOURCE_SYSTEM.DWCA_LINK
  );
  private final DSLContext context;

  public JobState addDownloadLinkToJob(ExportType exportType, String sourceSystemId,
      String downloadLink) {
    log.debug("Adding download link {} to job {}", downloadLink, sourceSystemId);
    var downloadLinkColumn = downloadLinkColumns.get(exportType);
    var result = context.update(Tables.SOURCE_SYSTEM)
        .set(downloadLinkColumn, downloadLink)
        .where(Tables.SOURCE_SYSTEM.ID.eq(sourceSystemId.replace(HANDLE_PROXY, "")))
        .execute();
    if (result > 0) {
      return JobState.COMPLETED;
    } else {
      log.warn("Failed to update job {} with download link {}", sourceSystemId, downloadLink);
      return JobState.FAILED;
    }
  }
}
