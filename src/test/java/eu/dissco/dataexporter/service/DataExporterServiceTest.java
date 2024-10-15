package eu.dissco.dataexporter.service;

import static eu.dissco.dataexporter.utils.TestUtils.CREATED;
import static eu.dissco.dataexporter.utils.TestUtils.HASHED_PARAMS;
import static eu.dissco.dataexporter.utils.TestUtils.ID;
import static eu.dissco.dataexporter.utils.TestUtils.MAPPER;
import static eu.dissco.dataexporter.utils.TestUtils.givenJobRequest;
import static eu.dissco.dataexporter.utils.TestUtils.givenJobResult;
import static eu.dissco.dataexporter.utils.TestUtils.givenScheduledJob;
import static eu.dissco.dataexporter.utils.TestUtils.givenUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

import eu.dissco.dataexporter.repository.DataExporterRepository;
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
  private AwsEmailService emailService;

  private DataExporterService service;

  private MockedStatic<Instant> mockedStatic;
  private MockedStatic<Clock> mockedClock;

  @BeforeEach
  void setup() {
    initTime();
    try {
      service = new DataExporterService(repository, emailService, MAPPER,
          MessageDigest.getInstance("MD5"));
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
  void testScheduleJob() throws Exception {

    try (var mockedUuid = mockStatic(UUID.class)) {
      // Given
      mockedUuid.when(UUID::randomUUID).thenReturn(ID);
      mockedUuid.when(() -> UUID.fromString(any())).thenReturn(HASHED_PARAMS);

      // When
      service.addJobToQueue(givenJobRequest(), givenUser());

      // Then
      then(repository).should().addJobToQueue(givenScheduledJob());
    }
  }

  @Test
  void testMarkJobAsRunning() {
    // Given

    // When
    service.markJobAsRunning(ID);

    // Then
    then(repository).should().markJobAsRunning(ID);
  }

  @Test
  void testMarkJobAsComplete() {
    // Given
    var jobResult = givenJobResult();

    // When
    service.markJobAsComplete(jobResult);

    // Then
    then(emailService).should().sendMail(jobResult);
    then(repository).should().markJobAsComplete(jobResult);
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
