package it.smartcommunitylabdhub.coreproxy.commons.mappers;

import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface BaseDataMapper<T extends AbstractBaseData> {

    String module();

    String getTableName();

    String[] getColumns();

    Object[] getValues(T item);

    Object[] getTypes(T item);

}
