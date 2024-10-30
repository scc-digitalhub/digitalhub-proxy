package it.smartcommunitylabdhub.coreproxy.commons.interfaces;

import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.List;

public interface MapperModule {

    String tableName();

    List<String> patterns(); //<Mapper>

    List<TableEntry> tableEntries();

    TableValue mapBody(BaseData baseData) throws SQLException;

}
