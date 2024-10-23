package it.smartcommunitylabdhub.coreproxy.commons.models;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseData implements Serializable {

    private Long id;
    private String path;
    private byte[] requestBody;
    private byte[] responseBody;
    private Map<String,String> requestHeaders;
    private Map<String,String> responseHeaders;
    private Instant requestTimestamp;
    private Instant responseTimestamp;
}
