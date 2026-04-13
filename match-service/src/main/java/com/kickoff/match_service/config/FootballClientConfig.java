package com.kickoff.match_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class FootballClientConfig {

    @Value("${football.api.key}")
    private String apiKey;

    @Bean
    public RestClient footballRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.football-data.org/v4")
                .defaultHeader("X-Auth-Token", apiKey)
                .build();
    }
}
