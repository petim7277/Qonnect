package com.example.qonnect.infrastructure.adapters.config.generalAppConfig;


import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.infrastructure.adapters.config.security.JwtAuthConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppBeanConfig {

    @Bean
    public JwtAuthConverter jwtAuthConverter(UserOutputPort userOutputPort) {
        return new JwtAuthConverter(userOutputPort);
    }


}
