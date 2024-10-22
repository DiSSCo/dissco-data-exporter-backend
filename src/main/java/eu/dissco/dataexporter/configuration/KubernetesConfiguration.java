package eu.dissco.dataexporter.configuration;

import eu.dissco.dataexporter.properties.KubernetesProperties;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({KubernetesProperties.class})
public class KubernetesConfiguration {

  private final KubernetesProperties properties;

  @Bean
  public BatchV1Api batchV1Api() throws IOException {
    var client = apiClient();
    return new BatchV1Api(client);
  }

  @Bean
  public ApiClient apiClient() throws IOException {
    var apiClient = Config.defaultClient();
    var httpClient = apiClient.getHttpClient().newBuilder()
        .retryOnConnectionFailure(true)
        .readTimeout(properties.getApiReadTimeout())
        .writeTimeout(properties.getApiWriteTimeout())
        .connectTimeout(properties.getApiConnectTimeout())
        .pingInterval(properties.getApiPingInterval())
        .build();
    apiClient.setHttpClient(httpClient);
    return apiClient;
  }

}
