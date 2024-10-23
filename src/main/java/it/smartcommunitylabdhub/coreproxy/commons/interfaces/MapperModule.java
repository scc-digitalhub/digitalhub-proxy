package it.smartcommunitylabdhub.coreproxy.commons.interfaces;

import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;
import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;

import java.util.List;

public interface MapperModule {

    List<String> paths(); //<Mapper>

    List<TableEntry> tableEntries();

    AbstractBaseData map(BaseData baseData);
}
