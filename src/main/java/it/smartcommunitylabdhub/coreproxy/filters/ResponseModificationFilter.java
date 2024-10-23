package it.smartcommunitylabdhub.coreproxy.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ResponseModificationFilter extends AbstractGatewayFilterFactory<ResponseModificationFilter.Config> implements Ordered {

    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ResponseModificationFilter(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory,
                                      ApplicationEventPublisher eventPublisher) {
        super(Config.class);
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
                            String requestPath = request.getURI().getPath();
                            log.info("Request Path: {}", requestPath);

                            // Log response headers
                            log.info("Response Headers:");
                            response.getHeaders().forEach((key, values) ->
                                    log.info("{}: {}", key, String.join(", ", values))
                            );

                            // Detect content type dynamically
                            MediaType contentType = response.getHeaders().getContentType();
                            log.info("Detected content type: {}", contentType);

//                            AntPathMatcher pathMatcher = new AntPathMatcher();
//                            if (pathMatcher.match("/", requestPath)) {
//                                BaseData baseData = new BaseData();
//                                eventPublisher.publishEvent(new DataBufferEvent(this, baseData));
//                            }

                            // Directly use the dataBuffer and publish it
                            DataBufferFactory dataBufferFactory = response.bufferFactory();
                            DataBuffer modifiedBodyResponse = dataBufferFactory.wrap(originalBodyResponse.asByteBuffer());

                            // Return the modified data buffer
                            return Mono.just(modifiedBodyResponse);
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
