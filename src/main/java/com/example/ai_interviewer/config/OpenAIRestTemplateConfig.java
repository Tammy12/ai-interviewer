package com.example.ai_interviewer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class OpenAIRestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${openai.baseUrl}")String url,
            @Value("${openai.apiKey}")String apiKey
    ) {
        return restTemplateBuilder
                .rootUri(url)
                .readTimeout(Duration.ofSeconds(30))
                .additionalInterceptors((ClientHttpRequestInterceptor) (request, body, execution) ->{
                    request.getHeaders().add("Authorization", "Bearer " + apiKey);
                    return execution.execute(request, body);
                }).build();
    }
}
