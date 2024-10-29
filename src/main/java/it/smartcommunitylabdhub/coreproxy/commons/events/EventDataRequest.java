package it.smartcommunitylabdhub.coreproxy.commons.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Map;

@Getter
public class EventDataRequest extends ApplicationEvent {

    private final String id;
    private final byte[] request;
    private final String path;
    private final String method;
    private final Map<String, String> headers;
    private final Instant instant;

    public EventDataRequest(Object source, String id,
                            byte[] request, String path, String method,
                            Map<String, String> headers,
                            Instant timestamp) {
        super(source);

        this.id = id;
        this.request = request;
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.instant = timestamp;
    }

}
