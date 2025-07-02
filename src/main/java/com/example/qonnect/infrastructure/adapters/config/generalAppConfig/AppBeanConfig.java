package com.example.qonnect.infrastructure.adapters.config.generalAppConfig;


import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.infrastructure.adapters.config.security.JwtAuthConverter;
import com.example.qonnect.infrastructure.adapters.output.keycloak.KeycloakAdapter;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppBeanConfig {

    @Bean
    public JwtAuthConverter jwtAuthConverter(UserOutputPort userOutputPort) {
        return new JwtAuthConverter(userOutputPort);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }



}
