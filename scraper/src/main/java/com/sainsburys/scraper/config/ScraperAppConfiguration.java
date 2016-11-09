package com.sainsburys.scraper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * The applications core configuration.
 */
@Configuration
public class ScraperAppConfiguration {

    /**
     * Configures a {@link ObjectMapper} to the context.
     *
     * @return the ObjectMapper to the context
     */
    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    /**
     * Configures a {@link Validator} to the context.
     *
     * @return the default validator
     */
    @Bean
    public Validator defaultValidator() {
        return new LocalValidatorFactoryBean();
    }

}
