package com.aidom.api.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.client.ClientConfiguration;

class ElasticsearchConfigTest {

  @Test
  @DisplayName("http URI로 올바른 호스트와 포트의 ClientConfiguration을 생성한다")
  void clientConfiguration_createsWithCorrectHost() throws Exception {
    ElasticsearchConfig config = createConfig("http://localhost:9200", "", "");

    ClientConfiguration clientConfig = config.clientConfiguration();

    InetSocketAddress endpoint = clientConfig.getEndpoints().get(0);
    assertThat(clientConfig.getEndpoints()).hasSize(1);
    assertThat(endpoint.getHostName()).isEqualTo("localhost");
    assertThat(endpoint.getPort()).isEqualTo(9200);
    assertThat(clientConfig.useSsl()).isFalse();
  }

  @Test
  @DisplayName("스킴 없는 URI도 정상 처리된다")
  void clientConfiguration_handlesUriWithoutScheme() throws Exception {
    ElasticsearchConfig config = createConfig("es-host:9200", "", "");

    ClientConfiguration clientConfig = config.clientConfiguration();

    InetSocketAddress endpoint = clientConfig.getEndpoints().get(0);
    assertThat(clientConfig.getEndpoints()).hasSize(1);
    assertThat(endpoint.getHostName()).isEqualTo("es-host");
    assertThat(endpoint.getPort()).isEqualTo(9200);
  }

  @Test
  @DisplayName("https URI는 SSL이 활성화된다")
  void clientConfiguration_enablesSslForHttps() throws Exception {
    ElasticsearchConfig config = createConfig("https://es-prod:9243", "", "");

    ClientConfiguration clientConfig = config.clientConfiguration();

    InetSocketAddress endpoint = clientConfig.getEndpoints().get(0);
    assertThat(endpoint.getHostName()).isEqualTo("es-prod");
    assertThat(endpoint.getPort()).isEqualTo(9243);
    assertThat(clientConfig.useSsl()).isTrue();
  }

  @Test
  @DisplayName("username과 password가 설정되면 Basic Auth 헤더가 포함된다")
  void clientConfiguration_appliesBasicAuth() throws Exception {
    ElasticsearchConfig config = createConfig("http://localhost:9200", "elastic", "changeme");

    ClientConfiguration clientConfig = config.clientConfiguration();

    assertThat(clientConfig.getDefaultHeaders().get("Authorization")).isNotEmpty();
  }

  @Test
  @DisplayName("username이 비어있으면 Basic Auth가 적용되지 않는다")
  void clientConfiguration_skipsAuthWhenEmpty() throws Exception {
    ElasticsearchConfig config = createConfig("http://localhost:9200", "", "");

    ClientConfiguration clientConfig = config.clientConfiguration();

    assertThat(clientConfig.getDefaultHeaders().get("Authorization")).isNull();
  }

  @Test
  @DisplayName("https URI에 포트가 없으면 443이 기본값으로 사용된다")
  void clientConfiguration_defaultsTo443ForHttps() throws Exception {
    ElasticsearchConfig config = createConfig("https://es-prod", "", "");

    ClientConfiguration clientConfig = config.clientConfiguration();

    assertThat(clientConfig.getEndpoints().get(0).getPort()).isEqualTo(443);
  }

  private ElasticsearchConfig createConfig(String uri, String username, String password)
      throws Exception {
    ElasticsearchConfig config = new ElasticsearchConfig();
    setField(config, "esUri", uri);
    setField(config, "username", username);
    setField(config, "password", password);
    return config;
  }

  private void setField(Object target, String fieldName, String value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
