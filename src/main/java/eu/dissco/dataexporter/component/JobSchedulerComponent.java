package eu.dissco.dataexporter.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.ExportJob;
import eu.dissco.dataexporter.exception.SchedulingFailedRuntimeException;
import eu.dissco.dataexporter.properties.JobProperties;
import eu.dissco.dataexporter.properties.TokenProperties;
import eu.dissco.dataexporter.repository.DataExporterRepository;
import eu.dissco.dataexporter.schema.SearchParam;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobSchedulerComponent {

  private final DataExporterRepository repository;
  private final JobProperties jobProperties;
  private final Template jobTemplate;
  private final BatchV1Api batchV1Api;
  @Qualifier("yamlMapper")
  private final ObjectMapper yamlMapper;
  private final TokenProperties tokenProperties;

  @Scheduled(fixedRate = 3600)
  public void schedule(){
    var runningJobs = repository.getRunningJobs();
    if (runningJobs < jobProperties.getQueueSize()){
      var nextJob = repository.getNextJobInQueue();
      if (nextJob.isPresent()){
        try {
          scheduleJob(nextJob.get());
        } catch (ApiException e){
          log.error("Failed to execute job {}", nextJob.get().id(), e);
          repository.updateJobState(nextJob.get().id(), JobState.FAILED);
        }
      }
    }
    log.debug("No jobs in queue");
  }

  private void scheduleJob(ExportJob exportJob) throws ApiException {
    log.info("Scheduling job {}", exportJob.id());
    var job = createV1Job(exportJob);
    batchV1Api.createNamespacedJob(jobProperties.getNamespace(), job).execute();
    repository.updateJobState(exportJob.id(), JobState.QUEUED);
    log.info("Successfully deployed job {}", exportJob.id());
  }

  private V1Job createV1Job(ExportJob exportJob){
    try {
      var properties = getDeploymentTemplateProperties(exportJob);
      var writer = new StringWriter();
      jobTemplate.process(properties, writer);
      return yamlMapper.readValue(writer.toString(), V1Job.class);
    } catch (TemplateException | IOException e){
      log.error("Error in scheduling job", e);
      throw new SchedulingFailedRuntimeException();
    }
  }

  private Map<String, String> getDeploymentTemplateProperties(ExportJob exportJob){
    var map = new HashMap<String, String>();
    map.put("jobName", exportJob.id().toString());
    map.put("namespace", jobProperties.getNamespace());
    map.put("jobId", exportJob.id().toString());
    map.put("image", jobProperties.getImage());
    map.put("bucketName", jobProperties.getBucketName());
    map.put("jobType", exportJob.exportType().toString().toLowerCase());
    map.put("inputValues", getParamTermList(exportJob, true));
    map.put("inputFields", getParamTermList(exportJob, false));
    map.put("targetType", exportJob.targetType().getName());
    map.put("endpointBackend", jobProperties.getEndpoint());
    map.put("tokenIdName", tokenProperties.getIdName());
    map.put("tokenSecretName", tokenProperties.getSecretName());
    return map;
  }

  private static String getParamTermList(ExportJob exportJob, boolean streamValues){
    List<String> termList;
    if (streamValues){
      termList = exportJob.params().stream()
          .map(SearchParam::getInputValue)
          .toList();
    } else {
      termList = exportJob.params().stream()
          .map(SearchParam::getInputField)
          .map(s -> s.replace("'", ""))
          .toList();
    }
    return String.join(",", termList);
  }
}
