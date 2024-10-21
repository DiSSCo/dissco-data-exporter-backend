package eu.dissco.dataexporter.service;

import eu.dissco.dataexporter.database.jooq.enums.JobState;
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

  private static final String SUBJECT = "Your DiSSCo Data Download is Ready!";
  private static final String SENDER = "no.reply.dissco@gmail.com";
  private static final String TEMPLATE =
      """
      Good day,
      <p>
      Your DiSSCo download job is ready at the following link: %s
      <p><p>
      Warm regards,
      <p>
      The DiSSCo development team
      """;

  public JobState sendAwsMail(String s3Link, String destinationEmail) {
    var emailRequest = SendEmailRequest.builder()
        .destination(destination -> destination
            .toAddresses(destinationEmail).build())
        .content(content -> content.simple(
            message -> message
                .subject(subject -> subject.data(SUBJECT))
                .body(body -> body
                    .html(bodyContent -> bodyContent
                        .data(String.format(TEMPLATE, s3Link))))))
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

}
