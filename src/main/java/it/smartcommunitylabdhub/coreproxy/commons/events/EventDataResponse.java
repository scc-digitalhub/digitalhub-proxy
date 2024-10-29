package it.smartcommunitylabdhub.coreproxy.commons.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Map;

@Getter
public class EventDataResponse extends ApplicationEvent {

    private final String id;
    private final byte[] response;
    private final String path;
    private final Map<String, String> headers;
    private final Instant instant;

    public EventDataResponse(Object source, String id, byte[] response,
                             String path,
                             Map<String, String> headers,
                             Instant timestamp) {
        super(source);

        this.id = id;
        this.response = response;
        this.path = path;
        this.headers = headers;
        this.instant = timestamp;
    }

}
