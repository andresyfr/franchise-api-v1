package com.andresyfr.franchise.infrastructure.config;

import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.observability.ContextProviderFactory;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;

/**
 * Observability wiring:
 * <ul>
 *   <li>Creates spans for every MongoDB command, linked to the HTTP request trace.</li>
 *   <li>Excludes actuator calls (health checks, scrapes) from traces and HTTP metrics.</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
public class ObservabilityConfig {

    private static final String ACTUATOR_PATH = "/actuator";

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoObservationCustomizer(ObservationRegistry observationRegistry) {
        return settings -> settings
                .contextProvider(ContextProviderFactory.create(observationRegistry))
                .addCommandListener(new MongoObservationCommandListener(observationRegistry));
    }

    @Bean
    public ObservationPredicate ignoreActuatorRequests() {
        return (observationName, context) ->
                !(context instanceof ServerRequestObservationContext serverContext
                        && serverContext.getCarrier().getPath().value().startsWith(ACTUATOR_PATH));
    }
}