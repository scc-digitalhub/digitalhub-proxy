package it.smartcommunitylabdhub.coreproxy.commons.events;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class RequestEventData extends ApplicationEvent {

    private final String uuid;
    private final byte[] body;
    private final Map<String,String> headers;

    public RequestEventData(Object source, String uuid, byte[] body, Map<String, String> headers) {
        super(source);

        this.uuid = uuid;
        this.body = body;
        this.headers = headers;
    }

}
