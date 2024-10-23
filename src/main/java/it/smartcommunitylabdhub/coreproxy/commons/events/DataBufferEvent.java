package it.smartcommunitylabdhub.coreproxy.commons.events;

import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@Getter
public class DataBufferEvent extends ApplicationEvent {

    private final BaseData baseData;

    public DataBufferEvent(Object source,
                           BaseData baseData) {
        super(source);
        this.baseData = baseData;
    }

}
