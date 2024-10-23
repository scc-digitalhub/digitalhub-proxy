package it.smartcommunitylabdhub.coreproxy.commons.converters;

import it.smartcommunitylabdhub.coreproxy.commons.interfaces.DataBufferConverter;
import it.smartcommunitylabdhub.coreproxy.modules.python.models.PythonData;
import org.springframework.stereotype.Component;


@Component
public class PythonDataBufferConverter implements DataBufferConverter<PythonData> {

    @Override
    public PythonData convert(byte[] dataBuffer) {
        return null;
    }
}
