package it.smartcommunitylabdhub.coreproxy.commons.converters;

import it.smartcommunitylabdhub.coreproxy.commons.interfaces.MapperModule;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableEntry;
import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;
import it.smartcommunitylabdhub.coreproxy.modules.text.models.TextData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component(value = "textMapperModule")
public class TextMapperModule implements MapperModule {

    @Override
    public List<String> paths() {
        return new ArrayList<>() {{

        }};
    }

    @Override
    public List<TableEntry> tableEntries() {
        return new ArrayList<>() {{
            add(new TableEntry("text", "VARCHAR(255)"));
        }};

    }

    @Override
    public TextData map(BaseData baseData) {
        return TextData.builder()
                .text(
                        new String(baseData.getRequestBody(), StandardCharsets.UTF_8)
                ).build();
    }
}
