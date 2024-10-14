package eu.dissco.dataexporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.ExportJob;
import eu.dissco.dataexporter.exception.InvalidRequestException;
import eu.dissco.dataexporter.repository.DataExporterRepository;
import eu.dissco.dataexporter.schema.ExportJobRequest;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataExporterService {

  private final DataExporterRepository repository;
  private final ObjectMapper mapper;
  private final MessageDigest messageDigest;

  public void addJobToQueue(ExportJobRequest jobRequest, String orcid)
      throws InvalidRequestException {
    var timestamp = Instant.now();
    var params = mapper.valueToTree(jobRequest.getData().getAttributes().getParams().getAdditionalProperties());
    var job = new ExportJob(
        UUID.randomUUID(),
        params,
        orcid,
        JobState.SCHEDULED,
        timestamp,
        null,
        null,
        ExportType.valueOf(jobRequest.getData().getAttributes().getExportType().toString()),
        hashParams(params)
    );
    repository.addJobToQueue(job);
  }

  private UUID hashParams(JsonNode params) throws InvalidRequestException {
    var hexString = new StringBuilder();
    try {
      messageDigest.update(mapper.writeValueAsBytes(params));
    } catch (JsonProcessingException e){
      log.error("Unable to parse Job params", e);
      throw new InvalidRequestException("Invalid job parameters");
    }
    var digest = messageDigest.digest();
    for (var b : digest) {
      hexString.append(String.format("%02x", b));
    }
    return UUID.fromString(hexString.toString().replaceFirst(
        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
        "$1-$2-$3-$4-$5"
    ));
  }

}
