package com.aidom.api.global.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(
    name = "spring.data.elasticsearch.repositories.enabled",
    havingValue = "true",
    matchIfMissing = false)
@EnableElasticsearchRepositories(basePackages = "com.aidom.api.domain")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

  @Value("${spring.elasticsearch.uris}")
  private String esUri;

  @Value("${spring.elasticsearch.username:}")
  private String username;

  @Value("${spring.elasticsearch.password:}")
  private String password;

  @Override
  public ClientConfiguration clientConfiguration() {
    String normalizedUri =
        esUri.startsWith("http://") || esUri.startsWith("https://") ? esUri : "http://" + esUri;
    URI uri = URI.create(normalizedUri);
    boolean useSsl = "https".equalsIgnoreCase(uri.getScheme());

    String host = uri.getHost();
    int port = uri.getPort();
    if (port == -1) {
      port = useSsl ? 443 : 9200;
    }
    String hostAndPort = host + ":" + port;

    var step = ClientConfiguration.builder().connectedTo(hostAndPort);
    var builder = useSsl ? step.usingSsl() : step;

    if (!username.isEmpty() && !password.isEmpty()) {
      builder = builder.withBasicAuth(username, password);
    }

    return builder.build();
  }
}
