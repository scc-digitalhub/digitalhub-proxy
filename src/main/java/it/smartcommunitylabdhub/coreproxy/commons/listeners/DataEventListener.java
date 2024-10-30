package it.smartcommunitylabdhub.coreproxy.commons.listeners;


import it.smartcommunitylabdhub.coreproxy.commons.events.EventDataRequest;
import it.smartcommunitylabdhub.coreproxy.commons.events.EventDataResponse;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.MapperModule;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableEntry;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;
import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;
import it.smartcommunitylabdhub.coreproxy.commons.repositories.DataDaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Component
@Slf4j
public class DataEventListener implements InitializingBean {


    private final MapperModule mapper;
    private final String prefix;
    private final DataDaoRepository dataDaoRepository;

    private final String requestTableName;
    private final String responseTableName;

    @Autowired
    public DataEventListener(@Value("${mapper.module}") String qualifier,
                             @Value("${mapper.prefix}") String prefix,
                             ApplicationContext applicationContext, DataDaoRepository dataDaoRepository) {

        this.mapper = (MapperModule) applicationContext.getBean(qualifier);
        if (this.mapper == null) {
            throw new IllegalArgumentException("No mapper found with qualifier: " + qualifier);
        }
        if (this.mapper.tableName() == null) {
            throw new IllegalArgumentException("No table name found for mapper: " + qualifier);
        }

        this.prefix = prefix;
        this.dataDaoRepository = dataDaoRepository;

        this.requestTableName = prefix + "request";
        this.responseTableName = prefix + "response";

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        List<TableEntry> tableEntries = mapper.tableEntries();
        tableEntries.forEach(tableEntry -> {
            String name = prefix + tableEntry.key();


            dataDaoRepository.createTable(name,
                    List.of(
                            new TableEntry("id", "VARCHAR(50)"),
                            new TableEntry("type", "VARCHAR(255)"),
                            new TableEntry("body", tableEntry.type())
                    ));
        });


        // Declare field for request and response
        List<TableEntry> reqOrRespEntry = List.of(
                new TableEntry("id", "VARCHAR(50)"),
                new TableEntry("tx_id", "VARCHAR(50)"),
                new TableEntry("path", "VARCHAR(255)"),
                new TableEntry("method", "VARCHAR(10)"),
                new TableEntry("timestamp", "TIMESTAMP")
        );

        // Create request and response table
        dataDaoRepository.createTable(requestTableName, reqOrRespEntry);
        dataDaoRepository.createTable(responseTableName, reqOrRespEntry);


    }


    @EventListener
    public void handleRequestEventData(EventDataRequest event) {

        try {
            log.info("Handling event: {}", event);

            String id = UUID.randomUUID().toString();

            BaseData baseData = BaseData.builder()
                    .id(id)
                    .txId(event.getTxId())
                    .path(event.getPath())
                    .method(event.getMethod())
                    .body(event.getBody())
                    .timestamp(event.getInstant())
                    .build();


            // Store request
            dataDaoRepository.insert(requestTableName, baseData.tableValues());


            // Map body and store body
            TableValue bodyTableValue = mapper.mapBody(baseData);
            dataDaoRepository.insert(prefix + bodyTableValue.key(), List.of(
                            new TableValue("id", Types.VARCHAR, id),
                            new TableValue("type", Types.VARCHAR, "request"),
                            bodyTableValue
                    )
            );

        } catch (SQLException e) {
            log.error("Error inserting data: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }


    @EventListener
    public void handleResponseEventData(EventDataResponse event) {

        try {
            log.info("Handling event: {}", event);

            String id = UUID.randomUUID().toString();

            BaseData baseData = BaseData.builder()
                    .id(id)
                    .txId(event.getTxId())
                    .path(event.getPath())
                    .method(event.getMethod())
                    .body(event.getBody())
                    .timestamp(event.getInstant())
                    .build();


            // Store request
            dataDaoRepository.insert(responseTableName, baseData.tableValues());


            // Map body and store body
            TableValue bodyTableValue = mapper.mapBody(baseData);
            dataDaoRepository.insert(prefix + bodyTableValue.key(), List.of(
                            new TableValue("id", Types.VARCHAR, id),
                            new TableValue("type", Types.VARCHAR, "response"),
                            bodyTableValue
                    )
            );
        } catch (SQLException e) {
            log.error("Error inserting data: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
