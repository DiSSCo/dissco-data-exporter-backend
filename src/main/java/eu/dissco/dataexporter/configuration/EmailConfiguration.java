package eu.dissco.dataexporter.configuration;

import eu.dissco.dataexporter.properties.AwsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
@RequiredArgsConstructor
public class EmailConfiguration {
  private final AwsProperties awsProperties;

  @Bean
  public SesV2Client s3Client(){
    var region = Region.EU_WEST_2;
    return SesV2Client.builder()
        .region(region)
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(awsProperties.getAccessKey(),
               awsProperties.getSecretAccessKey())))
        .build();
  }

}
