package it.smartcommunitylabdhub.coreproxy.commons.models;


import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;
import lombok.experimental.SuperBuilder;

import java.util.List;


@SuperBuilder
public abstract class AbstractBaseData extends BaseData {

    public abstract List<TableValue> tableValues();

}
