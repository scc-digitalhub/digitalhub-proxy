package it.smartcommunitylabdhub.coreproxy.commons.events;

import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class ResponseEventData extends ApplicationEvent {

   private final BaseData baseData;

    public ResponseEventData(Object source, BaseData baseData){
        super(source);
        this.baseData = baseData;
    }
}
