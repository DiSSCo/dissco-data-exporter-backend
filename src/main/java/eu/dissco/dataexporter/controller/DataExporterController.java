package eu.dissco.dataexporter.controller;

import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.JobResult;
import eu.dissco.dataexporter.domain.User;
import eu.dissco.dataexporter.exception.ForbiddenException;
import eu.dissco.dataexporter.exception.InvalidRequestException;
import eu.dissco.dataexporter.schema.DataExportRequest;
import eu.dissco.dataexporter.service.DataExporterService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@RestControllerAdvice
public class DataExporterController {

  private static final String ORCID = "orcid";

  private final DataExporterService service;

  private static User getUser(Authentication authentication) throws ForbiddenException {
    var claims = ((Jwt) authentication.getPrincipal()).getClaims();
    if (claims.containsKey(ORCID) && claims.containsKey("email")) {
      return new User(retrieveOrcid(claims), (String) claims.get("email"));
    } else {
      log.error("Missing ORCID or email in token");
      throw new ForbiddenException("Missing ORCID or email");
    }
  }

  private static JobState getJobState(String jobStateStr) throws InvalidRequestException {
    if (jobStateStr.equals("running")) {
      return JobState.RUNNING;
    } else if (jobStateStr.equals("failed")) {
      return JobState.FAILED;
    }
    log.error("Job state {} is not recognized", jobStateStr);
    throw new InvalidRequestException("Invalid job state :" + jobStateStr);
  }

  @Operation(summary = "Schedule a download job")
  @PostMapping(value = "schedule", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> scheduleJob(Authentication authentication,
      @RequestBody DataExportRequest request)
      throws ForbiddenException, InvalidRequestException {
    var user = getUser(authentication);
    service.handleJobRequest(request, user);
    log.info("Successfully processed job request");
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @PreAuthorize("hasRole('dissco-data-exporter-backend')")
  @Operation(summary = "Update a job state to running/failed. Used by data export job", hidden = true)
  @PostMapping("internal/{id}/{jobState}")
  public ResponseEntity<Void> updateJobState(@PathVariable("id") UUID id,
      @PathVariable("jobState") String jobStateStr) throws InvalidRequestException {
    service.updateJobState(id, getJobState(jobStateStr));
    log.info("Successfully marked job {} as {}", id, jobStateStr);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PreAuthorize("hasRole('dissco-data-exporter-backend')")
  @Operation(summary = "Update a job state to completed. Used by data export job", hidden = true)
  @PostMapping(value = "internal/completed", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> completeJob(@RequestBody JobResult jobResult) {
    service.markJobAsComplete(jobResult);
    log.info("Successfully marked job {} as complete", jobResult.id());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  private static String retrieveOrcid(Map<String, Object> claims) {
    var orcid = (String) claims.get(ORCID);
    if (!orcid.startsWith("https://orcid.org/")) {
      return "https://orcid.org/" + orcid;
    } else {
      return orcid;
    }
  }

}
