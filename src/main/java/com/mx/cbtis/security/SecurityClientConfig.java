package com.mx.cbtis.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SecurityClientConfig {

    @Bean
    public RestTemplate restTemplate(
            @Value("${security.internal-api-key:}") String internalApiKey) {
        RestTemplate template = new RestTemplate();
        String trimmedKey = internalApiKey.trim();
        if (!trimmedKey.isEmpty()) {
            template.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().set("X-Internal-Api-Key", trimmedKey);
                return execution.execute(request, body);
            });
        }
        return template;
    }
}
