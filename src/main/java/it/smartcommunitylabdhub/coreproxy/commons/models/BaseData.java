package it.smartcommunitylabdhub.coreproxy.commons.models;

import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseData implements Serializable {

    private String id;
    private String txId;
    private String path;
    private String method;
    private byte[] body;

    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    private Instant timestamp;

    public List<TableValue> tableValues() {

        return List.of(new TableValue("id", Types.VARCHAR, id),
                new TableValue("tx_id", Types.VARCHAR, txId),
                new TableValue("path", Types.VARCHAR, path),
                new TableValue("method", Types.VARCHAR, method),
                new TableValue("timestamp", Types.TIMESTAMP, Timestamp.from(timestamp)));
    }
}
