package eu.dissco.dataexporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.ExportJob;
import eu.dissco.dataexporter.domain.JobResult;
import eu.dissco.dataexporter.domain.TargetType;
import eu.dissco.dataexporter.domain.User;
import eu.dissco.dataexporter.exception.InvalidRequestException;
import eu.dissco.dataexporter.repository.DataExporterRepository;
import eu.dissco.dataexporter.repository.SourceSystemRepository;
import eu.dissco.dataexporter.schema.Attributes;
import eu.dissco.dataexporter.schema.DataExportRequest;
import eu.dissco.dataexporter.schema.SearchParam;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
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
  private final SourceSystemRepository sourceSystemRepository;

  private static String determineSourceSystemId(ExportJob exportJob)
      throws InvalidRequestException {
    var sourceSystemList = exportJob.params().stream().
        filter(param -> param.getInputField().contains("ods:sourceSystemID"))
        .map(SearchParam::getInputValue)
        .toList();
    if (sourceSystemList.size() != 1) {
      log.error("Source system ID not found or multiple IDs found for job {} with params: {}",
          exportJob.id(), exportJob.params());
      throw new InvalidRequestException("Source system ID not found or multiple IDs found");
    } else {
      return sourceSystemList.getFirst();
    }
  }

  public void handleJobRequest(DataExportRequest jobRequest, User user)
      throws InvalidRequestException {
    var params = jobRequest.getData().getAttributes().getSearchParams();
    var hashedParams = hashParams(params);
    addJobToQueue(jobRequest.getData().getAttributes(), hashedParams, user);
  }

  private void checkIfJobIsValid(ExportJob job) throws InvalidRequestException {
    var isSourceSystemJob = job.isSourceSystemJob();
    var exportType = job.exportType();
    if (isSourceSystemJob && exportType.equals(ExportType.DOI_LIST)) {
      throw new InvalidRequestException("Invalid export type for source system job: " + exportType);
    } else if (isSourceSystemJob) {
      var sourceSystemId = determineSourceSystemId(job);
      log.info("This is a source system job with source system ID: {}", sourceSystemId);
    }
  }

  private void addJobToQueue(Attributes jobAttributes, UUID hashedParams, User user)
      throws InvalidRequestException {
    var timestamp = Instant.now();
    var job = new ExportJob(
        UUID.randomUUID(),
        jobAttributes.getSearchParams(),
        user.orcid(),
        JobState.SCHEDULED,
        timestamp,
        null,
        null,
        ExportType.valueOf(jobAttributes.getExportType().toString()),
        hashedParams,
        user.email(),
        TargetType.fromString(jobAttributes.getTargetType().toString()),
        Boolean.TRUE.equals(jobAttributes.getIsSourceSystemJob()),
        null);
    checkIfJobIsValid(job);
    repository.addJobToQueue(job);
    log.info("Successfully added job {} to queue", job.id());
  }

  public void updateJobState(UUID id, JobState jobState) {
    repository.updateJobState(id, jobState);
  }

  public void markJobAsComplete(JobResult jobResult) {
    var exportJob = repository.getExportJob(jobResult.id());
    JobState jobState;
    if (exportJob.isSourceSystemJob()) {
      jobState = handleSourceSystemJob(jobResult, exportJob);
    } else {
      jobState = emailService.sendAwsMail(jobResult.downloadLink(), exportJob);
    }
    repository.markJobAsComplete(jobResult, jobState);
  }

  private JobState handleSourceSystemJob(JobResult jobResult, ExportJob exportJob) {
    JobState jobState;
    try {
      var sourceSystemId = determineSourceSystemId(exportJob);
      jobState = sourceSystemRepository.addDownloadLinkToJob(exportJob.exportType(), sourceSystemId,
          jobResult.downloadLink());
    } catch (InvalidRequestException e) {
      log.error("SourceSystem Job: {} contains invalid params: {}", exportJob.id(),
          exportJob.params(), e);
      jobState = JobState.FAILED;
    }
    return jobState;
  }

  private UUID hashParams(List<SearchParam> params) throws InvalidRequestException {
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
