package it.smartcommunitylabdhub.coreproxy.commons.converters;

import it.smartcommunitylabdhub.coreproxy.commons.interfaces.MapperModule;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableEntry;
import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;
import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;
import it.smartcommunitylabdhub.coreproxy.modules.text.models.TextData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
            add(new TableEntry("request", "VARCHAR(255)"));
            add(new TableEntry("response", "VARCHAR(255)"));
        }};

    }

    @Override
    public AbstractBaseData mapResponse(BaseData baseData) {
        TextData textData = new TextData();

        textData.mapBaseFields(baseData);
        textData.setResponse(new String(baseData.getResponseBody(),
                StandardCharsets.UTF_8));

        return textData;
    }

    @Override
    public TextData mapRequest(BaseData baseData) {
        TextData textData = new TextData();

        textData.mapBaseFields(baseData);
        textData.setRequest(new String(baseData.getRequestBody(),
                StandardCharsets.UTF_8));
        textData.setResponse(new String(baseData.getResponseBody(),
                StandardCharsets.UTF_8));

        return textData;
    }
}
