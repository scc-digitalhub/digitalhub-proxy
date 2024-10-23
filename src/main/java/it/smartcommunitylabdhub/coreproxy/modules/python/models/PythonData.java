package it.smartcommunitylabdhub.coreproxy.modules.python.models;

import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;
import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.sql.Types;
import java.util.List;

@SuperBuilder
@Getter
@Setter
public class PythonData extends AbstractBaseData {

    private String json;

    @Override
    public List<TableValue> tableValues() {
        return List.of(
                new TableValue("json", Types.OTHER, json)
        );
    }
}
