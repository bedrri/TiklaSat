package com.gib.tiklasat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Outbox olaylarını (BR-N-007) JSON'a çevirmek için kullanılan ObjectMapper.
 * Bu projede spring-boot-starter-webmvc, standart spring-boot-starter-web'in
 * getirdiği otomatik Jackson yapılandırmasını sağlamıyor — bu yüzden burada
 * elle tanımlanır.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
