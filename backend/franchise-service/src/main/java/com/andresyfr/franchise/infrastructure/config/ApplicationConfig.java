package com.andresyfr.franchise.infrastructure.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides application-wide dependencies that can be replaced in tests.
 */
@Configuration(proxyBeanMethods = false)
public class ApplicationConfig {

    /**
     * Supplies a UTC clock for deterministic time-dependent behavior.
     * @return application clock
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}