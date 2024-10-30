package it.smartcommunitylabdhub.coreproxy.filters;

import it.smartcommunitylabdhub.coreproxy.commons.events.EventDataRequest;
import it.smartcommunitylabdhub.coreproxy.commons.events.EventDataResponse;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.MapperModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
public class ResponseModificationFilter extends AbstractGatewayFilterFactory<ResponseModificationFilter.Config> implements Ordered {

    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final MapperModule mapper;

    private final AntPathMatcher matcher = new AntPathMatcher();


    @Autowired
    public ResponseModificationFilter(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory,
                                      ApplicationEventPublisher eventPublisher,
                                      @Value("${mapper.module}") String qualifier, ApplicationContext applicationContext) {
        super(ResponseModificationFilter.Config.class);

        // Retrieve the mapper
        this.mapper = (MapperModule) applicationContext.getBean(qualifier);

        if (this.mapper == null) {
            throw new IllegalArgumentException("No mapper found with qualifier: " + qualifier);
        }

        this.modifyResponseBodyFilterFactory = modifyResponseBodyFilterFactory;
        this.eventPublisher = eventPublisher;

    }

    @Override
    public GatewayFilter apply(Config config) {

        return modifyResponseBodyFilterFactory.apply(
                new ModifyResponseBodyGatewayFilterFactory.Config()
                        .setRewriteFunction(DataBuffer.class, DataBuffer.class, (exchange, originalBodyResponse) -> {
                            ServerHttpResponse response = exchange.getResponse();
                            ServerHttpRequest request = exchange.getRequest();

                            // Log request path and other relevant information
                            String path = request.getURI().getPath();
                            log.info("Request Path: {}", path);

                            // Log response headers
                            log.info("Response Headers:");
                            response.getHeaders().forEach((key, values) ->
                                    log.info("{}: {}", key, String.join(", ", values))
                            );

                            log.info("Request Headers:");
                            request.getHeaders().forEach((key, values) ->
                                    log.info("{}: {}", key, String.join(", ", values))
                            );


                            // Extract and log the request method (GET, POST, etc.)
                            String method = request.getMethod().name();
                            log.info("Request Method: {}", method);



                            boolean hasMatchingPattern = mapper.patterns().stream()
                                    .anyMatch(pattern -> matcher.match(pattern, path));

                            String txId = request.getHeaders().getFirst("tx-id");


                            if (originalBodyResponse.readableByteCount() == 0) {
                                log.warn("Original response body is empty or null. Returning empty DataBuffer but send event to store response.");

                                if (txId != null && hasMatchingPattern) {

                                    // Add the tx-id header and publish the event
                                    response.getHeaders().add("tx-id", txId);

                                    eventPublisher.publishEvent(new EventDataResponse(
                                            this,
                                            txId,
                                            new byte[0],
                                            path,
                                            method,
                                            response.getHeaders().toSingleValueMap(),
                                            Instant.now()
                                    ));
                                }
                            }


                            // Read the original response body
                            return Mono.fromCallable(() -> {
                                // Read bytes from the original DataBuffer
                                byte[] bytes = new byte[originalBodyResponse.readableByteCount()];
                                originalBodyResponse.read(bytes);
                                DataBufferUtils.release(originalBodyResponse); // Release the buffer after reading

                                // Log the original response body
                                log.info("Original Response Body: {}", new String(bytes, StandardCharsets.UTF_8));


                                if (txId != null && hasMatchingPattern) {

                                    // Add the tx-id header and publish the event
                                    response.getHeaders().add("tx-id", txId);

                                    eventPublisher.publishEvent(new EventDataResponse(
                                            this,
                                            txId,
                                            bytes,
                                            path,
                                            method,
                                            response.getHeaders().toSingleValueMap(),
                                            Instant.now()
                                    ));
                                }

                                // Return the modified DataBuffer
                                return response.bufferFactory().wrap(bytes);
                            });


                        }));

    }

    public static class Config {
        // Configuration properties (if needed)
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // You can set this to any order you prefer
    }
}
