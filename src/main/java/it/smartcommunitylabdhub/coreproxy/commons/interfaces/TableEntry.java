package it.smartcommunitylabdhub.coreproxy.commons.interfaces;

import java.sql.SQLType;

// ogni entry viene usata per generare una tabella
// quindi (text, varchar ) genera la tabell s_body_text ( id(lo stesso id della richesta o della risposta, ReqOrResp, body)
public record TableEntry(String key, String type) {
}
