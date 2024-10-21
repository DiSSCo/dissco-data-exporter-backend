package eu.dissco.dataexporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.ExportJob;
import eu.dissco.dataexporter.domain.JobResult;
import eu.dissco.dataexporter.domain.User;
import eu.dissco.dataexporter.exception.InvalidRequestException;
import eu.dissco.dataexporter.repository.DataExporterRepository;
import eu.dissco.dataexporter.schema.Attributes;
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
  private final EmailService emailService;
  private final ObjectMapper mapper;
  private final MessageDigest messageDigest;

  public void handleJobRequest(ExportJobRequest jobRequest, User user)
      throws InvalidRequestException {
    var params = mapper.valueToTree(
        jobRequest.getData().getAttributes().getParams().getAdditionalProperties());
    var hashedParams = hashParams(params);
    var existingS3Link = repository.getJobResultsIfExists(hashedParams);
    if (existingS3Link.isPresent()) {
      log.info("Job with params {} has already been executed. Notifying user.", params);
      var result = emailService.sendAwsMail(existingS3Link.get(), user.email());
      if (result.equals(JobState.NOTIFICATION_FAILED)) {
        log.error("Failed to notify user of job. Scheduling separate job");
        addJobToQueue(params, hashedParams, user,
            jobRequest.getData().getAttributes().getExportType());
      }
    } else {
      addJobToQueue(params, hashedParams, user,
          jobRequest.getData().getAttributes().getExportType());
    }
  }

  private void addJobToQueue(JsonNode params, UUID hashedParams, User user,
      Attributes.ExportType exportType)
      throws InvalidRequestException {
    var timestamp = Instant.now();
    var job = new ExportJob(
        UUID.randomUUID(),
        params,
        user.orcid(),
        JobState.SCHEDULED,
        timestamp,
        null,
        null,
        ExportType.valueOf(exportType.toString()),
        hashedParams,
        user.email()
    );
    repository.addJobToQueue(job);
  }

  public void updateJobState(UUID id, JobState jobState) {
    repository.updateJobState(id, jobState);
  }

  public void markJobAsComplete(JobResult jobResult) {
    var email = repository.getUserEmailFromJobId(jobResult.id());
    var jobState = emailService.sendAwsMail(jobResult.downloadLink(), email);
    repository.markJobAsComplete(jobResult, jobState);
  }

  private UUID hashParams(JsonNode params) throws InvalidRequestException {
    var hexString = new StringBuilder();
    try {
      messageDigest.update(mapper.writeValueAsBytes(params));
    } catch (JsonProcessingException e) {
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
