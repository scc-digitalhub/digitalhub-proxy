package it.smartcommunitylabdhub.coreproxy.modules.text.models;

import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;
import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@SuperBuilder
public class TextData extends AbstractBaseData {
    private String text;

    @Override
    public List<TableValue> tableValues() {
        return new ArrayList<>() {{
            add(new TableValue("text", Types.VARCHAR, text));
        }};
    }
}
