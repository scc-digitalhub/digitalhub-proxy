package it.smartcommunitylabdhub.coreproxy.filters;

import it.smartcommunitylabdhub.coreproxy.commons.events.EventData;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.MapperModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class RequestModificationFilter extends AbstractGatewayFilterFactory<RequestModificationFilter.Config> implements Ordered {

    private final ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilterFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final MapperModule mapper;

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Autowired
    public RequestModificationFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilterFactory,
                                     ApplicationEventPublisher eventPublisher,
                                     @Value("${mapper.module}") String qualifier, ApplicationContext applicationContext) {
        super(Config.class);

        // Retrieve the mapper
        this.mapper = (MapperModule) applicationContext.getBean(qualifier);
        if (this.mapper == null) {
            throw new IllegalArgumentException("No mapper found with qualifier: " + qualifier);
        }

        this.modifyRequestBodyFilterFactory = modifyRequestBodyFilterFactory;
        this.eventPublisher = eventPublisher;

    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            // Generate a UUID for x-session-id
            String xSessionId = UUID.randomUUID().toString();

            // Add the x-session-id header to the request
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("x-session-id", xSessionId)
                    .build();

            log.info("Generated x-session-id: {}", xSessionId);

            // Replace the original exchange with the one containing the modified request
            return modifyRequestBodyFilterFactory.apply(
                    new ModifyRequestBodyGatewayFilterFactory.Config()
                            .setRewriteFunction(DataBuffer.class, DataBuffer.class, (exchange1, originalRequestBody) -> {
                                // Extract and log request path and headers
                                ServerHttpRequest request = exchange1.getRequest();

                                // Log request path
                                String path = request.getURI().getPath();
                                log.info("Request Path: {}", path);

                                // Log request headers, including the new x-session-id
                                log.info("Request Headers:");
                                request.getHeaders().forEach((key, values) ->
                                        log.info("{}: {}", key, String.join(", ", values))
                                );

                                // Define a new DataBufferFactory for the modified request body
                                DataBufferFactory dataBufferFactory = exchange1.getResponse().bufferFactory();

                                // Handle null or empty DataBuffer (originalRequestBody)
                                if (originalRequestBody == null) {
                                    log.warn("Original request body is empty or null. Returning empty DataBuffer.");
                                    // Return an empty DataBuffer if the original body is null or empty
                                    return Mono.just(dataBufferFactory.wrap(new byte[0]));
                                }

                                // Read the original request body
                                return DataBufferUtils.join(Flux.just(originalRequestBody))
                                        .flatMap(dataBuffer -> {
                                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                            dataBuffer.read(bytes);
                                            DataBufferUtils.release(dataBuffer); // Release the buffer after reading

                                            boolean hasMatchingPattern = mapper.patterns().stream()
                                                    .anyMatch(pattern -> matcher.match(pattern, path));

                                            // Perform an action based on whether there is a matching path
                                            if (hasMatchingPattern) {
                                                // Publish event to event bus
                                                eventPublisher.publishEvent(new EventData(
                                                        this,
                                                        xSessionId,
                                                        new byte[0],
                                                        bytes,
                                                        path,
                                                        request.getHeaders().toSingleValueMap(),
                                                        Instant.now()
                                                ));
                                            }

                                            // Return the original DataBuffer unchanged
                                            DataBuffer newDataBuffer = dataBufferFactory.wrap(bytes);
                                            return Mono.just(newDataBuffer);
                                        });
                            })
            ).filter(exchange.mutate().request(modifiedRequest).build(), chain);  // Mutate the exchange with the modified request
        };
    }

    public static class Config {
        // Configuration properties (if needed)
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // You can set this to any order you prefer
    }
}
