package eu.dissco.dataexporter.controller;

import eu.dissco.dataexporter.domain.User;
import eu.dissco.dataexporter.domain.JobResult;
import eu.dissco.dataexporter.exception.ForbiddenException;
import eu.dissco.dataexporter.exception.InvalidRequestException;
import eu.dissco.dataexporter.schema.ExportJobRequest;
import eu.dissco.dataexporter.service.DataExporterService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
@Slf4j
@RequiredArgsConstructor
public class DataExporterController {

  private final DataExporterService service;

  @PostMapping("/schedule")
  public ResponseEntity<Void> scheduleJob(Authentication authentication,
      @RequestBody ExportJobRequest request)
      throws ForbiddenException, InvalidRequestException {
    var user = getUser(authentication);
    service.addJobToQueue(request, user);
    log.info("Successfully posted job to queue");
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @PostMapping("/{id}/running")
  public ResponseEntity<Void> markJobAsRunning(@PathVariable("id") UUID id) {
    service.markJobAsRunning(id);
    log.info("Successfully marked job {} as running", id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping(value ="/completed", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> completeJob(@RequestBody JobResult jobResult) {
    service.markJobAsComplete(jobResult);
    log.info("Successfully marked job {} as complete", jobResult.id());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  private static User getUser(Authentication authentication) throws ForbiddenException {
    var claims = ((Jwt) authentication.getPrincipal()).getClaims();
    if (claims.containsKey("orcid") && claims.containsKey("email")) {
      return new User((String) claims.get("orcid"), (String) claims.get("email"));
    } else {
      log.error("Missing ORCID or email in token");
      throw new ForbiddenException("Missing ORCID or email");
    }
  }

}
