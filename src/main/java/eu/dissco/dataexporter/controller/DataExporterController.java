package eu.dissco.dataexporter.controller;

import eu.dissco.dataexporter.exception.ForbiddenException;
import eu.dissco.dataexporter.exception.InvalidRequestException;
import eu.dissco.dataexporter.schema.ExportJobRequest;
import eu.dissco.dataexporter.service.DataExporterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
@Slf4j
@RequiredArgsConstructor
public class DataExporterController {

  private final DataExporterService service;

  @PostMapping("schedule")
  public ResponseEntity<Void> scheduleJob(Authentication authentication, @RequestBody ExportJobRequest request)
      throws ForbiddenException, InvalidRequestException {
    var orcid = getOrcid(authentication);
    service.addJobToQueue(request, orcid);
    log.info("Successfully posted job to queue");
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  /*
  How to see if space is available -> queue
  Export job: need to do it through streaming, either stream it to a local file or s3 append
  How to automatically trigger -> kafka? cron job?
   */


  private static String getOrcid(Authentication authentication) throws ForbiddenException {
    var claims = ((Jwt) authentication.getPrincipal()).getClaims();
    if (claims.containsKey("orcid")) {
      return (String) claims.get("orcid");
    } else {
      log.error("Missing ORCID in token");
      throw new ForbiddenException("No ORCID provided");
    }
  }

}
