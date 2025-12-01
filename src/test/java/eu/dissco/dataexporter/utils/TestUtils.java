package eu.dissco.dataexporter.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.ExportJob;
import eu.dissco.dataexporter.domain.JobResult;
import eu.dissco.dataexporter.domain.TargetType;
import eu.dissco.dataexporter.domain.User;
import eu.dissco.dataexporter.schema.Attributes;
import eu.dissco.dataexporter.schema.Attributes.ExportType;
import eu.dissco.dataexporter.schema.Data;
import eu.dissco.dataexporter.schema.DataExportRequest;
import eu.dissco.dataexporter.schema.SearchParam;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestUtils {

  public static final ObjectMapper MAPPER;
  public static final UUID ID = UUID.fromString("cd5c9ee7-23b1-4615-993e-9d56d0720213");
  public static final Instant CREATED = Instant.parse("2022-11-01T09:59:24.00Z");
  public static final String ORCID = "https://orcid.org/0000-0001-7573-4330";
  public static final String BARE_ORCID = "0000-0001-7573-4330";
  public static final String EMAIL = "noreply@dissco.eu";
  public static final UUID HASHED_PARAMS = UUID.fromString("cdecac99-021f-54a6-7656-cfbdc59059b4");
  public static final String DOWNLOAD_LINK = "https://aws.download/s3";
  public static final String SUBJECT = "Your DiSSCo Data Download is Ready!";
  public static final String SOURCE_SYSTEM_ID = "https://hdl.handle.net/TEST/CPX-0AF-717";

  static {
    var mapper = new ObjectMapper().findAndRegisterModules();
    var dateModule = new SimpleModule();
    dateModule.addSerializer(Date.class, new DateSerializer());
    dateModule.addDeserializer(Date.class, new DateDeserializer());
    mapper.registerModule(dateModule);
    mapper.setSerializationInclusion(Include.NON_NULL);
    MAPPER = mapper;
  }

  private TestUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static Map<String, Object> givenClaims() {
    return Map.of(
        "orcid", BARE_ORCID,
        "email", EMAIL);
  }

  public static DataExportRequest givenJobRequest(){
    return givenJobRequest(ExportType.DOI_LIST, false, givenSearchParams());
  }

  public static DataExportRequest givenJobRequest(ExportType exportType, boolean isSourceSystemJob, List<SearchParam> searchParams) {
    return new DataExportRequest().withData(new Data()
        .withType("export-job")
        .withAttributes(new Attributes()
            .withExportType(exportType)
            .withSearchParams(searchParams)
            .withIsSourceSystemJob(isSourceSystemJob)
            .withTargetType(
                Attributes.TargetType.HTTPS_DOI_ORG_21_T_11148_894_B_1_E_6_CAD_57_E_921764_E)));
  }

  public static List<SearchParam> givenSearchParams() {
    return List.of(new SearchParam()
        .withInputField("$['ods:organisationID']")
        .withInputValue("https://ror.org/0566bfb96"));
  }

  public static List<SearchParam> givenSourceSystemSearchParams() {
    return List.of(new SearchParam()
        .withInputField("$['ods:sourceSystemID']")
        .withInputValue(SOURCE_SYSTEM_ID));
  }

  public static ExportJob givenScheduledJob() {
    return givenScheduledJob(false, givenSearchParams(),
        eu.dissco.dataexporter.database.jooq.enums.ExportType.DOI_LIST);
  }

  public static ExportJob givenScheduledJob(boolean isSourceSystemJob,
      List<SearchParam> searchParams,
      eu.dissco.dataexporter.database.jooq.enums.ExportType exportType) {
    return new ExportJob(
        ID,
        searchParams,
        ORCID,
        JobState.SCHEDULED,
        CREATED,
        null,
        null,
        exportType,
        HASHED_PARAMS,
        EMAIL,
        TargetType.DIGITAL_SPECIMEN,
        isSourceSystemJob,
        null);
  }

  public static ExportJob givenCompletedJob() {
    return new ExportJob(
        ID,
        givenSearchParams(),
        ORCID,
        JobState.COMPLETED,
        CREATED,
        CREATED,
        CREATED,
        eu.dissco.dataexporter.database.jooq.enums.ExportType.DOI_LIST,
        HASHED_PARAMS,
        EMAIL,
        TargetType.DIGITAL_SPECIMEN,
        Boolean.FALSE,
        DOWNLOAD_LINK);
  }


  public static User givenUser() {
    return new User(ORCID, EMAIL);
  }

  public static JobResult givenJobResult() {
    return new JobResult(ID, DOWNLOAD_LINK);
  }
}
