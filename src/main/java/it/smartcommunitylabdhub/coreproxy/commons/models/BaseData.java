package it.smartcommunitylabdhub.coreproxy.commons.models;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseData implements Serializable {

    private String id;
    private String path;
    private byte[] requestBody;
    private byte[] responseBody;

    @Builder.Default
    private Map<String,String> requestHeaders = new HashMap<>();

    @Builder.Default
    private Map<String,String> responseHeaders = new HashMap<>();
    private Instant timestamp;


    // Protected setters to prevent access from subclasses
    protected void setId(String id) {
        this.id = id;
    }

    protected void setPath(String path) {
        this.path = path;
    }

    protected void setRequestBody(byte[] requestBody) {
        this.requestBody = requestBody;
    }

    protected void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
    }

    protected void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    protected void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    protected void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
