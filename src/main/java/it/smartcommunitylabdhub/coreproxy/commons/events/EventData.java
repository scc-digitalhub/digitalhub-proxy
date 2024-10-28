package it.smartcommunitylabdhub.coreproxy.commons.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Map;

@Getter
public class EventData extends ApplicationEvent {

    private final String id;
    private final byte[] response;
    private final byte[] request;
    private final String path;
    private final Map<String, String> headers;
    private final Instant requestTimestamp;

    public EventData(Object source, String id, byte[] response,
                     byte[] request, String path,
                     Map<String, String> headers,
                     Instant timestamp) {
        super(source);

        this.id = id;
        this.response = response;
        this.request = request;
        this.path = path;
        this.headers = headers;
        this.requestTimestamp = timestamp;
    }

}
