// Новый исправленный код
package dev.shrkptv.apigateway.config;

import org.springdoc.core.configuration.SpringDocDataRestConfiguration;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Configuration
@Import(SpringDocDataRestConfiguration.class)
public class OpenApiConfig {

    private final RouteDefinitionLocator locator;

    public OpenApiConfig(RouteDefinitionLocator locator) {
        this.locator = locator;
    }

    @Bean
    @Primary
    public CommandLineRunner openApiGroups(
            SwaggerUiConfigParameters swaggerUiConfigParameters
    ) {
        return args -> {
            List<RouteDefinition> routeDefinitions = Objects.requireNonNull(locator
                    .getRouteDefinitions()
                    .collectList()
                    .block());

            routeDefinitions.stream()
                    .filter(routeDefinition -> routeDefinition.getMetadata().containsKey("springdoc"))
                    .sorted(Comparator.comparing(RouteDefinition::getId))
                    .forEach(routeDefinition -> {
                        Map<String, Object> springdocMetadata =
                                (Map<String, Object>) routeDefinition.getMetadata().get("springdoc");

                        String groupName = (String) springdocMetadata.get("group");

                        if (groupName != null) {
                            swaggerUiConfigParameters.addGroup(groupName);
                        }
                    });
        };
    }
}