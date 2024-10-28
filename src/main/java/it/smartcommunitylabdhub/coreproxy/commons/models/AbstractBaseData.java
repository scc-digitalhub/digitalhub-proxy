package it.smartcommunitylabdhub.coreproxy.commons.models;


import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public abstract class AbstractBaseData extends BaseData {

    public abstract List<TableValue> tableValues();


    // Map base fields
    public void mapBaseFields(BaseData baseData) {
        this.setId(baseData.getId());
        this.setPath(baseData.getPath());
        this.setRequestBody(baseData.getRequestBody());
        this.setResponseBody(baseData.getResponseBody());
        this.setRequestHeaders(baseData.getRequestHeaders());
        this.setResponseHeaders(baseData.getResponseHeaders());
        this.setTimestamp(baseData.getTimestamp());
    }

}
