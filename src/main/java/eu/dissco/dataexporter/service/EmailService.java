package eu.dissco.dataexporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.ExportJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final SesV2Client emailClient;
  private final ObjectMapper mapper;

  private static final String SUBJECT = "Your DiSSCo Data Download is Ready!";
  private static final String SENDER = "noreply@dissco.eu";
  private static final String SUCCESSFUL_TEMPLATE =
      """
      Good day,
      <p>
      Your DiSSCo download job is ready at the following link: %s
      <p><p>
      Warm regards,
      <p>
      The DiSSCo development team
      """;

  private static final String NOT_FOUND_TEMPLATE =
      """
      Good day,
      <p>
      You requested a DiSSCo data export based on the following criteria:
      <p>
      %s
      These search parameters yielded no results. Consider broadening your search.
      <p><p>
      Warm regards,
      <p>
      The DiSSCo development team
      """;

  public JobState sendAwsMail(String s3Link, ExportJob exportJob) {
    var emailRequest = SendEmailRequest.builder()
        .destination(destination -> destination
            .toAddresses(exportJob.destinationEmail()).build())
        .content(content -> content.simple(
            message -> message
                .subject(subject -> subject.data(SUBJECT))
                .body(body -> body
                    .html(bodyContent -> bodyContent
                        .data(getEmailContent(s3Link, exportJob))))))
        .fromEmailAddress(SENDER)
        .build();
    try {
      emailClient.sendEmail(emailRequest);
      log.info("Successfully notified user of successful job");
      return JobState.COMPLETED;
    } catch (SesV2Exception e) {
      log.error("Unable to send email to recipient.", e);
      return JobState.NOTIFICATION_FAILED;
    }
  }

  private String getEmailContent(String s3Link, ExportJob exportJob){
    if (s3Link == null){
      String paramString;
      try {
        var searchParametersJson = mapper.createObjectNode();
        for (var param : exportJob.params()){
          searchParametersJson.put(param.getInputField(), param.getInputValue());
        }
        paramString = mapper.writeValueAsString(searchParametersJson);
      } catch (JsonProcessingException e) {
        log.warn("Unable to parse params as json", e);
        paramString = exportJob.params().toString();
      }
      return String.format(NOT_FOUND_TEMPLATE, paramString);
    }
    return String.format(SUCCESSFUL_TEMPLATE, s3Link);
  }
}
