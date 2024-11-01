package eu.dissco.dataexporter.component;

import static eu.dissco.dataexporter.utils.TestUtils.ID;
import static eu.dissco.dataexporter.utils.TestUtils.givenScheduledJob;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.domain.TargetType;
import eu.dissco.dataexporter.properties.JobProperties;
import eu.dissco.dataexporter.repository.DataExporterRepository;
import freemarker.template.Template;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobSchedulerComponentTest {

  @Mock
  private DataExporterRepository repository;
  @Mock
  private Template jobTemplate;
  @Mock
  private BatchV1Api batchV1Api;
  @Mock
  private JobProperties jobProperties;
  @Mock
  private ObjectMapper mockYamlMapper;
  private JobSchedulerComponent jobScheduler;

  private static final Integer JOB_QUEUE_SIZE = 3;
  private static final String NAMESPACE = "namespace";

  @BeforeEach
  void setup() {
    jobScheduler = new JobSchedulerComponent(repository, jobProperties, jobTemplate,
        batchV1Api, mockYamlMapper);
    given(jobProperties.getQueueSize()).willReturn(JOB_QUEUE_SIZE);
  }

  @Test
  void testQueueIsFull() {
    // Given
    given(repository.getRunningJobs()).willReturn(JOB_QUEUE_SIZE);

    // When
    jobScheduler.schedule();

    // Then
    then(repository).shouldHaveNoMoreInteractions();
    then(batchV1Api).shouldHaveNoInteractions();
  }

  @Test
  void testNoJobsRunning() {
    // Given
    given(repository.getRunningJobs()).willReturn(0);
    given(repository.getNextJobInQueue()).willReturn(Optional.empty());

    // When
    jobScheduler.schedule();

    // Then
    then(batchV1Api).shouldHaveNoInteractions();
  }

  @Test
  void testScheduleJob() throws Exception {
    // Given
    var exportJob = givenScheduledJob();
    given(repository.getRunningJobs()).willReturn(0);
    given(jobProperties.getNamespace()).willReturn(NAMESPACE);
    given(jobProperties.getImage()).willReturn("image");
    given(repository.getNextJobInQueue()).willReturn(Optional.of(exportJob));
    var properties = givenExpectedTemplateProperties();

    // When
    jobScheduler.schedule();

    // Then
    then(jobTemplate).should().process(eq(properties), any());
    then(batchV1Api).should().createNamespacedJob(eq(NAMESPACE), any());
  }

  private static Map<String, String> givenExpectedTemplateProperties() {
    var expectedTemplateProperties = new HashMap<String, String>();
    expectedTemplateProperties.put("jobName", ID.toString());
    expectedTemplateProperties.put("namespace", NAMESPACE);
    expectedTemplateProperties.put("jobId", ID.toString());
    expectedTemplateProperties.put("image", "image");
    expectedTemplateProperties.put("jobType", ExportType.DOI_LIST.getName());
    expectedTemplateProperties.put("inputValues", "https://ror.org/0566bfb96");
    expectedTemplateProperties.put("inputFields", "$['ods:organisationID']");
    expectedTemplateProperties.put("targetType", TargetType.DIGITAL_SPECIMEN.getName());
    return expectedTemplateProperties;
  }

}
