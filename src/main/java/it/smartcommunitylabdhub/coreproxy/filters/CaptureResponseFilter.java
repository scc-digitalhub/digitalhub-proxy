package it.smartcommunitylabdhub.coreproxy.filters;


import it.smartcommunitylabdhub.coreproxy.commons.events.DataBufferEvent;
import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;
import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;
import it.smartcommunitylabdhub.coreproxy.modules.text.models.TextData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;


@Slf4j
@Component
public class CaptureResponseFilter extends AbstractGatewayFilterFactory<CaptureResponseFilter.Config> implements Ordered {

    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public CaptureResponseFilter(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory, ApplicationEventPublisher eventPublisher) {
        super(Config.class);

        this.modifyResponseBodyFilterFactory = modifyResponseBodyFilterFactory;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public GatewayFilter apply(Config config) {

        final ModifyResponseBodyGatewayFilterFactory.Config modifyResponseBodyFilterFactoryConfig = new ModifyResponseBodyGatewayFilterFactory.Config();

        modifyResponseBodyFilterFactoryConfig.setRewriteFunction(DataBuffer.class, DataBuffer.class, (exchange, data) -> {
            ServerHttpResponse response = exchange.getResponse();
            ServerHttpRequest request = exchange.getRequest();


            // Create the BaseData object to collect request/response information
            BaseData baseData = new BaseData();

            // Set the path and timestamps (request timestamp)
            baseData.setPath(request.getURI().getPath());
            baseData.setRequestTimestamp(Instant.now()); // Set request timestamp when the request is processed

            // Capture request headers
            baseData.setRequestHeaders(request.getHeaders().toSingleValueMap());


            return DataBufferUtils.join(exchange.getRequest().getBody())
                    .flatMap(requestBuffer -> {

                        byte[] requestBodyBytes = new byte[requestBuffer.readableByteCount()];
                        requestBuffer.read(requestBodyBytes);
                        baseData.setRequestBody(requestBodyBytes); // Store request body
                        DataBufferUtils.release(requestBuffer); // Release buffer if no longer needed

                        // Now handle the response body
                        byte[] responseBodyBytes = new byte[data.readableByteCount()];
                        data.read(responseBodyBytes);
                        baseData.setResponseBody(responseBodyBytes); // Store response body

                        // Log response body (optional)
                        String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);
                        log.info("Response Body: {}", responseBody);

                        // Capture response headers and set response timestamp
                        baseData.setResponseHeaders(response.getHeaders().toSingleValueMap());
                        baseData.setResponseTimestamp(Instant.now()); // Set response timestamp

                        // Conditional event publishing based on request path
                        AntPathMatcher pathMatcher = new AntPathMatcher();
                        if (pathMatcher.match("/**", baseData.getPath())) {
                            // Publish event asynchronously with all collected data
                            eventPublisher.publishEvent(new DataBufferEvent(this, baseData));
                        }

                        // Return the modified response DataBuffer
                        DataBufferFactory dataBufferFactory = response.bufferFactory();
                        DataBuffer outputDataBuffer = dataBufferFactory.wrap(responseBodyBytes);

                        return Mono.just(outputDataBuffer);
                    });

        });

        return modifyResponseBodyFilterFactory.apply(modifyResponseBodyFilterFactoryConfig);
    }


//    @Override
//    public GatewayFilter apply(Config config) {
//
//        final ModifyResponseBodyGatewayFilterFactory.Config modifyResponseBodyFilterFactoryConfig = new ModifyResponseBodyGatewayFilterFactory.Config();
//
//        modifyResponseBodyFilterFactoryConfig.setRewriteFunction(DataBuffer.class, DataBuffer.class, (exchange, dataBuffer) -> {
//            ServerHttpResponse response = exchange.getResponse();
//            var headers = response.getHeaders().entrySet();
//            log.info("Headers:");
//            response.getHeaders().forEach((key, values) ->
//                    log.info("{}: {}", key, String.join(", ", values))
//            );
//
//            // Detect content type dynamically
//            MediaType contentType = response.getHeaders().getContentType();
//            log.info("Detected content type: {}", contentType);
//
//            // Read the data buffer as a String
//            Mono<String> bodyAsString = DataBufferUtils.join(Flux.just(dataBuffer))
//                    .map(buffer -> {
//                        byte[] bytes = new byte[buffer.readableByteCount()];
//                        buffer.read(bytes);
//                        DataBufferUtils.release(buffer); // Important to release the buffer
//                        return new String(bytes, StandardCharsets.UTF_8);
//                    });
//
//            return bodyAsString.flatMap(body -> {
//
//
//                log.info("Body: {}", body);
//
//                // Create a new DataBufferFactory to create the modified response body
//                DataBufferFactory dataBufferFactory = response.bufferFactory();
//
//
//                return Mono.just(dataBufferFactory.wrap(body.getBytes(StandardCharsets.UTF_8)));
//
//
//            });
//        });
//
//        return modifyResponseBodyFilterFactory.apply(modifyResponseBodyFilterFactoryConfig);
//
//    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public static class Config {
        // Configuration properties (if needed)
    }
}
