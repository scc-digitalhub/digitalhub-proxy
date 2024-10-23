package it.smartcommunitylabdhub.coreproxy.commons.interfaces;

import java.io.Serializable;

public record TableValue(String key, int type, Serializable value) {
}
