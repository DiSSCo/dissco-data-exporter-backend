package eu.dissco.dataexporter.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.ExportJob;
import eu.dissco.dataexporter.domain.JobResult;
import eu.dissco.dataexporter.domain.User;
import eu.dissco.dataexporter.schema.Attributes;
import eu.dissco.dataexporter.schema.Attributes.ExportType;
import eu.dissco.dataexporter.schema.Data;
import eu.dissco.dataexporter.schema.ExportJobRequest;
import eu.dissco.dataexporter.schema.Params;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class TestUtils {

  private TestUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static final ObjectMapper MAPPER;
  public static final UUID ID = UUID.fromString("cd5c9ee7-23b1-4615-993e-9d56d0720213");
  public static final Instant CREATED = Instant.parse("2022-11-01T09:59:24.00Z");
  public static final String ORCID = "https://orcid.org/0000-0001-7573-4330";
  public static final String EMAIL = "example.email@gmail.com";
  public static final UUID HASHED_PARAMS = UUID.fromString("cdecac99-021f-54a6-7656-cfbdc59059b4");
  public static final String DOWNLOAD_LINK = "https://aws.download/s3";
  public static final String SUBJECT = "Your DiSSCo Data Download is Ready!";

  static {
    var mapper = new ObjectMapper().findAndRegisterModules();
    var dateModule = new SimpleModule();
    dateModule.addSerializer(Date.class, new DateSerializer());
    dateModule.addDeserializer(Date.class, new DateDeserializer());
    mapper.registerModule(dateModule);
    mapper.setSerializationInclusion(Include.NON_NULL);
    MAPPER = mapper;
  }

  public static Map<String, Object> givenClaims() {
    return Map.of(
        "orcid", ORCID,
        "email", EMAIL);
  }

  public static ExportJobRequest givenJobRequest() {
    return new ExportJobRequest().withData(new Data()
        .withType("export-job")
        .withAttributes(new Attributes()
            .withExportType(ExportType.DOI_LIST)
            .withParams(givenParams())));

  }

  public static Params givenParams() {
    return new Params().withAdditionalProperty("$['ods:organisationID']",
        "https://ror.org/0566bfb96");
  }

  public static JsonNode givenPramsJson() {
    return MAPPER.valueToTree(givenParams().getAdditionalProperties());
  }

  public static ExportJob givenScheduledJob() {
    return new ExportJob(
        ID,
        givenPramsJson(),
        ORCID,
        JobState.SCHEDULED,
        CREATED,
        null,
        null,
        eu.dissco.dataexporter.database.jooq.enums.ExportType.doi_list,
        HASHED_PARAMS,
        EMAIL
    );
  }

  public static User givenUser(){
    return new User(ORCID, EMAIL);
  }

  public static JobResult givenJobResult() {
    return new JobResult(ID, DOWNLOAD_LINK);
  }
}
