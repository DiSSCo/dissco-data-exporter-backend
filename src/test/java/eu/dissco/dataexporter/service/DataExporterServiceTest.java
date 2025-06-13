package eu.dissco.dataexporter.service;

import static eu.dissco.dataexporter.utils.TestUtils.CREATED;
import static eu.dissco.dataexporter.utils.TestUtils.DOWNLOAD_LINK;
import static eu.dissco.dataexporter.utils.TestUtils.HASHED_PARAMS;
import static eu.dissco.dataexporter.utils.TestUtils.ID;
import static eu.dissco.dataexporter.utils.TestUtils.MAPPER;
import static eu.dissco.dataexporter.utils.TestUtils.SOURCE_SYSTEM_ID;
import static eu.dissco.dataexporter.utils.TestUtils.givenJobRequest;
import static eu.dissco.dataexporter.utils.TestUtils.givenJobResult;
import static eu.dissco.dataexporter.utils.TestUtils.givenScheduledJob;
import static eu.dissco.dataexporter.utils.TestUtils.givenSearchParams;
import static eu.dissco.dataexporter.utils.TestUtils.givenSourceSystemSearchParams;
import static eu.dissco.dataexporter.utils.TestUtils.givenUser;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.exception.InvalidRequestException;
import eu.dissco.dataexporter.repository.DataExporterRepository;
import eu.dissco.dataexporter.repository.SourceSystemRepository;
import eu.dissco.dataexporter.schema.Attributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataExporterServiceTest {

  @Mock
  private DataExporterRepository repository;
  @Mock
  private EmailService emailService;
  @Mock
  private SourceSystemRepository sourceSystemRepository;

  private DataExporterService service;

  private MockedStatic<Instant> mockedStatic;
  private MockedStatic<Clock> mockedClock;

  @BeforeEach
  void setup() {
    initTime();
    try {
      service = new DataExporterService(repository, emailService, MAPPER,
          MessageDigest.getInstance("MD5"), sourceSystemRepository);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException();
    }
  }

  @AfterEach
  void destroy() {
    mockedStatic.close();
    mockedClock.close();
  }

  @Test
  void testScheduleNewJob() throws Exception {
    try (var mockedUuid = mockStatic(UUID.class)) {
      // Given
      mockedUuid.when(UUID::randomUUID).thenReturn(ID);
      mockedUuid.when(() -> UUID.fromString(any())).thenReturn(HASHED_PARAMS);

      // When
      service.handleJobRequest(givenJobRequest(), givenUser());

      // Then
      then(repository).should().addJobToQueue(givenScheduledJob());
    }
  }

  @Test
  void testScheduleNewSourceSystemJob() throws InvalidRequestException {
    try (var mockedUuid = mockStatic(UUID.class)) {
      // Given
      mockedUuid.when(UUID::randomUUID).thenReturn(ID);
      mockedUuid.when(() -> UUID.fromString(any())).thenReturn(HASHED_PARAMS);

      // When
      service.handleJobRequest(
          givenJobRequest(Attributes.ExportType.DWC_DP, true, givenSourceSystemSearchParams()),
          givenUser());

      // Then
      then(repository).should().addJobToQueue(
          givenScheduledJob(true, givenSourceSystemSearchParams(), ExportType.DWC_DP));
    }
  }


  @Test
  void testScheduleNewSourceSystemJobInvalidSearchParams() {
    try (var mockedUuid = mockStatic(UUID.class)) {
      // Given
      mockedUuid.when(UUID::randomUUID).thenReturn(ID);
      mockedUuid.when(() -> UUID.fromString(any())).thenReturn(HASHED_PARAMS);

      // When
      assertThrows(InvalidRequestException.class, () -> service.handleJobRequest(
          givenJobRequest(Attributes.ExportType.DWC_DP, true, givenSearchParams()),
          givenUser()));

      // Then
      then(repository).shouldHaveNoInteractions();
    }
  }

  @Test
  void testScheduleNewSourceSystemJobInvalidExportType() {
    try (var mockedUuid = mockStatic(UUID.class)) {
      // Given
      mockedUuid.when(UUID::randomUUID).thenReturn(ID);
      mockedUuid.when(() -> UUID.fromString(any())).thenReturn(HASHED_PARAMS);

      // When
      assertThrows(InvalidRequestException.class, () -> service.handleJobRequest(
          givenJobRequest(Attributes.ExportType.DOI_LIST, true, givenSearchParams()),
          givenUser()));

      // Then
      then(repository).shouldHaveNoInteractions();
    }
  }


  @Test
  void testUpdateJobState() {
    // Given

    // When
    service.updateJobState(ID, JobState.RUNNING);

    // Then
    then(repository).should().updateJobState(ID, JobState.RUNNING);
  }

  @Test
  void testMarkJobAsComplete() {
    // Given
    var jobResult = givenJobResult();
    given(repository.getExportJob(ID)).willReturn(givenScheduledJob());
    given(emailService.sendAwsMail(jobResult.downloadLink(), givenScheduledJob())).willReturn(
        JobState.COMPLETED);

    // When
    service.markJobAsComplete(jobResult);

    // Then
    then(repository).should().markJobAsComplete(jobResult, JobState.COMPLETED);
  }

  @Test
  void testMarkJobAsCompleteSourceSystem() {
    // Given
    var jobResult = givenJobResult();
    given(repository.getExportJob(ID)).willReturn(
        givenScheduledJob(true, givenSourceSystemSearchParams(), ExportType.DWC_DP));
    given(sourceSystemRepository.addDownloadLinkToJob(ExportType.DWC_DP, SOURCE_SYSTEM_ID,
        DOWNLOAD_LINK)).willReturn(JobState.COMPLETED);

    // When
    service.markJobAsComplete(jobResult);

    // Then
    then(repository).should().markJobAsComplete(jobResult, JobState.COMPLETED);
    then(emailService).shouldHaveNoInteractions();
  }

  @Test
  void testMarkJobAsCompleteSourceSystemIllegalParams() {
    // Given
    var jobResult = givenJobResult();
    given(repository.getExportJob(ID)).willReturn(
        givenScheduledJob(true, givenSearchParams(), ExportType.DWC_DP));

    // When
    service.markJobAsComplete(jobResult);

    // Then
    then(repository).should().markJobAsComplete(jobResult, JobState.FAILED);
    then(emailService).shouldHaveNoInteractions();
  }

  @Test
  void testMarkJobAsCompleteFailedToNotify() {
    // Given
    var jobResult = givenJobResult();
    given(repository.getExportJob(ID)).willReturn(givenScheduledJob());
    given(emailService.sendAwsMail(jobResult.downloadLink(), givenScheduledJob())).willReturn(
        JobState.NOTIFICATION_FAILED);

    // When
    service.markJobAsComplete(jobResult);

    // Then
    then(repository).should().markJobAsComplete(jobResult, JobState.NOTIFICATION_FAILED);
  }

  private void initTime() {
    Clock clock = Clock.fixed(CREATED, ZoneOffset.UTC);
    mockedClock = mockStatic(Clock.class);
    mockedClock.when(Clock::systemUTC).thenReturn(clock);
    Instant instant = Instant.now(clock);
    mockedStatic = mockStatic(Instant.class);
    mockedStatic.when(Instant::now).thenReturn(instant);
    mockedStatic.when(() -> Instant.from(any())).thenReturn(instant);
  }

}
