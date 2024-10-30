package it.smartcommunitylabdhub.coreproxy.commons.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Map;

@Getter
public class EventDataResponse extends ApplicationEvent {

    private final String txId;
    private final byte[] body;
    private final String path;
    private final String method;
    private final Map<String, String> headers;
    private final Instant instant;

    public EventDataResponse(Object source,
                             String txId,
                             byte[] body,
                             String path, String method,
                             Map<String, String> headers,
                             Instant timestamp) {
        super(source);

        this.txId = txId;
        this.body = body;
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.instant = timestamp;
    }

}
