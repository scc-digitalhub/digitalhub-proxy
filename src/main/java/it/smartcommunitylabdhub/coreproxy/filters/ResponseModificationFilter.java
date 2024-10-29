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


                            // Read the original response body and convert it to a String
                            return DataBufferUtils.join(Flux.just(originalBodyResponse))
                                    .flatMap(dataBuffer -> {
                                        // Read bytes from the DataBuffer
                                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(bytes);
                                        DataBufferUtils.release(dataBuffer); // Release the buffer after reading

                                        // Optionally log or manipulate the bytes
                                        String responseBody = new String(bytes, StandardCharsets.UTF_8);
                                        log.info("Original Response Body: {}", responseBody);


                                        String xSessionId = request.getHeaders()
                                                .getFirst("x-session-id");

                                        boolean hasMatchingPattern = mapper.patterns().stream()
                                                .anyMatch(pattern -> matcher.match(pattern, path));

                                        if (hasMatchingPattern && xSessionId != null) {

                                            // Add the x-session-id header to the response
                                            response.getHeaders().add("x-session-id", xSessionId);

                                            // Publish event to event bus
                                            eventPublisher.publishEvent(new EventDataResponse(
                                                    this,
                                                    xSessionId,
                                                    bytes,
                                                    path,
                                                    response.getHeaders().toSingleValueMap(),
                                                    Instant.now()
                                            ));
                                        }


                                        // Wrap the bytes back into a DataBuffer
                                        DataBufferFactory dataBufferFactory = response.bufferFactory();
                                        DataBuffer modifiedBodyResponse = dataBufferFactory.wrap(bytes);

                                        // Return the modified DataBuffer
                                        return Mono.just(modifiedBodyResponse);
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
