package eu.dissco.dataexporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DataExporter {
  public static void main(String[] args) {
    SpringApplication.run(DataExporter.class, args);
  }

}
