package it.smartcommunitylabdhub.coreproxy.filters;

import it.smartcommunitylabdhub.coreproxy.commons.events.RequestEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class RequestModificationFilter extends AbstractGatewayFilterFactory<RequestModificationFilter.Config> implements Ordered {

    private final ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilterFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public RequestModificationFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilterFactory, ApplicationEventPublisher eventPublisher) {
        super(Config.class);
        this.modifyRequestBodyFilterFactory = modifyRequestBodyFilterFactory;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            // Generate a UUID for x-session-id
            String sessionId = UUID.randomUUID().toString();

            // Add the x-session-id header to the request
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("x-session-id", sessionId)
                    .build();

            log.info("Generated x-session-id: {}", sessionId);


            // Replace the original exchange with the one containing the modified request
            return modifyRequestBodyFilterFactory.apply(
                    new ModifyRequestBodyGatewayFilterFactory.Config()
                            .setRewriteFunction(DataBuffer.class, DataBuffer.class, (exchange1, originalRequestBody) -> {
                                // Extract and log request path and headers
                                ServerHttpRequest request = exchange1.getRequest();

                                // Log request path
                                String requestPath = request.getURI().getPath();
                                log.info("Request Path: {}", requestPath);

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

                                            // Publish event to event bus
                                            eventPublisher.publishEvent(new RequestEventData(
                                                    this,
                                                    sessionId,
                                                    bytes,
                                                    request.getHeaders().toSingleValueMap()
                                            ));
//                                            String originalBodyString = new String(bytes, StandardCharsets.UTF_8);
//                                            log.info("Original request body: {}", originalBodyString);

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
