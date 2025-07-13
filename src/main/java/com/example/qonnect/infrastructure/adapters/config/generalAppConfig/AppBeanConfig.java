package com.example.qonnect.infrastructure.adapters.config.generalAppConfig;


import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.services.EmailService;
import com.example.qonnect.infrastructure.adapters.config.security.JwtAuthConverter;
import com.example.qonnect.infrastructure.adapters.output.keycloak.KeycloakAdapter;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public EmailService emailService(JavaMailSender javaMailSender){
//        return new EmailService(javaMailSender);
//    }



}
