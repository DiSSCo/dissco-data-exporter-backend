package eu.dissco.dataexporter.component;

import eu.dissco.dataexporter.properties.QueueProperties;
import eu.dissco.dataexporter.repository.DataExporterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobSchedulerComponent {

  private final DataExporterRepository repository;
  private final QueueProperties queueProperties;

  @Scheduled(fixedRate = 3600)
  public void schedule(){
    var runningJobs = repository.getRunningJobs();
    if (runningJobs < queueProperties.getSize()){
      // Schedule job
    }


  }


}
