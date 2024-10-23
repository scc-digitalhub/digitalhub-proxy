package it.smartcommunitylabdhub.coreproxy.commons.interfaces;

import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;

public interface DataBufferConverter<T extends AbstractBaseData> {
    T convert(byte[] dataBuffer);

}