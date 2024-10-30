package it.smartcommunitylabdhub.coreproxy.commons.converters;

import it.smartcommunitylabdhub.coreproxy.commons.interfaces.MapperModule;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableEntry;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;
import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

//TODO try jsonb and vector

@Slf4j
@Component(value = "textMapperModule")
public class TextMapperModule implements MapperModule {
    @Override
    public String tableName() {
        return "text_result";
    }

    @Override
    public List<String> patterns() {
        return new ArrayList<>() {{
            add("/**");
        }};
    }

    @Override
    public List<TableEntry> tableEntries() {
        return new ArrayList<>() {{
            add(new TableEntry("body", "TEXT"));
            add(new TableEntry("text_json", "JSONB"));
        }};

    }

    public TableValue mapBody(BaseData baseData) throws SQLException {

        // TODO base on the path create the PGobject body
        PGobject body = new PGobject();
        body.setType("TEXT");
        body.setValue(new String(baseData.getBody(), StandardCharsets.UTF_8));

        return new TableValue("body", Types.OTHER, body);
    }
}
