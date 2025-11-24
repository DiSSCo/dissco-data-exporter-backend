package eu.dissco.dataexporter.component;

import static eu.dissco.dataexporter.utils.TestUtils.ID;
import static eu.dissco.dataexporter.utils.TestUtils.givenScheduledJob;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.TargetType;
import eu.dissco.dataexporter.properties.JobProperties;
import eu.dissco.dataexporter.properties.TokenProperties;
import eu.dissco.dataexporter.repository.DataExporterRepository;
import freemarker.template.Template;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.BatchV1Api.APIcreateNamespacedJobRequest;
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
  @Mock
  private TokenProperties tokenProperties;
  private JobSchedulerComponent jobScheduler;

  private static final Integer JOB_QUEUE_SIZE = 3;
  private static final String NAMESPACE = "namespace";

  @BeforeEach
  void setup() {
    jobScheduler = new JobSchedulerComponent(repository, jobProperties, jobTemplate,
        batchV1Api, mockYamlMapper, tokenProperties);
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
    then(repository).shouldHaveNoMoreInteractions();
  }

  @Test
  void testScheduleJob() throws Exception {
    // Given
    var exportJob = givenScheduledJob();
    var jobRequestMock = mock(APIcreateNamespacedJobRequest.class);
    given(repository.getRunningJobs()).willReturn(0);
    given(jobProperties.getNamespace()).willReturn(NAMESPACE);
    given(jobProperties.getImage()).willReturn("image");
    given(jobProperties.getBucketName()).willReturn("bucket");
    given(jobProperties.getEndpoint()).willReturn("endpoint");
    given(repository.getNextJobInQueue()).willReturn(Optional.of(exportJob));
    given(batchV1Api.createNamespacedJob(eq(NAMESPACE), any())).willReturn(jobRequestMock);
    given(tokenProperties.getIdName()).willReturn("tokenIdName");
    given(tokenProperties.getSecretName()).willReturn("tokenSecretName");
    given(tokenProperties.getTokenEndpoint()).willReturn("https://tokenEndpoint");
    var properties = givenExpectedTemplateProperties();

    // When
    jobScheduler.schedule();

    // Then
    then(jobTemplate).should().process(eq(properties), any());
    then(batchV1Api).should().createNamespacedJob(eq(NAMESPACE), any());
    then(repository).should().updateJobState(ID, JobState.QUEUED);
  }

  @Test
  void testScheduleJobFailed() throws Exception {
    // Given
    var jobRequestMock = mock(APIcreateNamespacedJobRequest.class);
    var exportJob = givenScheduledJob();
    given(repository.getRunningJobs()).willReturn(0);
    given(jobProperties.getNamespace()).willReturn(NAMESPACE);
    given(repository.getNextJobInQueue()).willReturn(Optional.of(exportJob));
    given(batchV1Api.createNamespacedJob(eq(NAMESPACE), any())).willReturn(jobRequestMock);
    doThrow(ApiException.class).when(jobRequestMock).execute();

    // When
    jobScheduler.schedule();

    // Then
    then(repository).should().updateJobState(ID, JobState.FAILED);
  }

  private static Map<String, String> givenExpectedTemplateProperties() {
    var expectedTemplateProperties = new HashMap<String, String>();
    expectedTemplateProperties.put("jobName", ID.toString());
    expectedTemplateProperties.put("namespace", NAMESPACE);
    expectedTemplateProperties.put("jobId", ID.toString());
    expectedTemplateProperties.put("image", "image");
    expectedTemplateProperties.put("jobType", "doi_list");
    expectedTemplateProperties.put("bucketName", "bucket");
    expectedTemplateProperties.put("inputValues", "https://ror.org/0566bfb96");
    expectedTemplateProperties.put("inputFields", "$[ods:organisationID]");
    expectedTemplateProperties.put("targetType", TargetType.DIGITAL_SPECIMEN.getName());
    expectedTemplateProperties.put("endpointBackend", "endpoint");
    expectedTemplateProperties.put("tokenEndpoint", "https://tokenEndpoint");
    expectedTemplateProperties.put("tokenIdName", "tokenIdName");
    expectedTemplateProperties.put("tokenSecretName", "tokenSecretName");
    expectedTemplateProperties.put("isSourceSystemJob", "false");
    expectedTemplateProperties.put("databaseUrl", null);
    return expectedTemplateProperties;
  }

}
