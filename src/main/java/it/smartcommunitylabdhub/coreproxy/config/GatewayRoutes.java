package it.smartcommunitylabdhub.coreproxy.config;

import it.smartcommunitylabdhub.coreproxy.filters.RequestModificationFilter;
import it.smartcommunitylabdhub.coreproxy.filters.ResponseModificationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GatewayRoutes {

    @Value("${gateway.route.id}")
    private String id;

    @Value("${gateway.route.uri}")
    private String uri;

    @Value("${gateway.route.path}")
    private String path;

    private final RequestModificationFilter requestModificationFilter;
    private final ResponseModificationFilter responseModificationFilter;

    public GatewayRoutes(RequestModificationFilter requestModificationFilter,
                         ResponseModificationFilter responseModificationFilter) {
        this.requestModificationFilter = requestModificationFilter;
        this.responseModificationFilter = responseModificationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(id, r -> r.path(path)
                        .filters(f -> f
                                .filter(requestModificationFilter.apply(new RequestModificationFilter.Config()))
                                .filter(responseModificationFilter.apply(new ResponseModificationFilter.Config()))
                        )
                        .uri(uri)
                )
                .build();
    }
}
