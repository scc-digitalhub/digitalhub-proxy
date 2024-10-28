package it.smartcommunitylabdhub.coreproxy.commons.interfaces;

import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;
import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;

import java.util.List;

public interface MapperModule {

    String tableName();

    List<String> patterns(); //<Mapper>

    List<TableEntry> tableEntries();

    AbstractBaseData mapRequest(BaseData baseData);

    AbstractBaseData mapResponse(BaseData baseData);

}
