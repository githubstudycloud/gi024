package com.example.mcp.acl.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * ACL 相关 Bean 配置。
 */
@Configuration
@EnableConfigurationProperties(LegacySystemProperties.class)
public class LegacyAclConfiguration {

    @Bean
    RestClient.Builder legacyRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient legacyRestClient(RestClient.Builder legacyRestClientBuilder, LegacySystemProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        return legacyRestClientBuilder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
