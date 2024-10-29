package it.smartcommunitylabdhub.coreproxy.commons.listeners;


import it.smartcommunitylabdhub.coreproxy.commons.events.EventDataRequest;
import it.smartcommunitylabdhub.coreproxy.commons.events.EventDataResponse;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.MapperModule;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableEntry;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;
import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;
import it.smartcommunitylabdhub.coreproxy.commons.models.BaseData;
import it.smartcommunitylabdhub.coreproxy.commons.repositories.DataDaoRepository;
import it.smartcommunitylabdhub.coreproxy.commons.utils.HashGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@Slf4j
public class DataEventListener implements InitializingBean {


    private final MapperModule mapper;
    private final DataDaoRepository dataDaoRepository;

    @Autowired
    public DataEventListener(@Value("${mapper.module}") String qualifier,
                             ApplicationContext applicationContext, DataDaoRepository dataDaoRepository) {

        this.mapper = (MapperModule) applicationContext.getBean(qualifier);
        if (this.mapper == null) {
            throw new IllegalArgumentException("No mapper found with qualifier: " + qualifier);
        }
        if (this.mapper.tableName() == null) {
            throw new IllegalArgumentException("No table name found for mapper: " + qualifier);
        }
        this.dataDaoRepository = dataDaoRepository;

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //TODO: create table on db.
        List<TableEntry> tableEntries = mapper.tableEntries();
        tableEntries.addAll(0, List.of(
                new TableEntry("id", "VARCHAR(50)"),
                new TableEntry("path", "VARCHAR(255)"),
                new TableEntry("method", "VARCHAR(10)"),
                new TableEntry("timestamp", "TIMESTAMP")
        ));

        String tableName = "serve_" + HashGenerator.generateHash(mapper.tableName());
        dataDaoRepository.createTable(tableName, tableEntries);

    }


    @EventListener
    public void handleRequestEventData(EventDataRequest event) {

        log.info("Handling event: {}", event);

        BaseData baseData = BaseData.builder()
                .id(event.getId())
                .path(event.getPath())
                .method(event.getMethod())
                .requestBody(event.getRequest())
                .requestHeaders(event.getHeaders())
                .timestamp(event.getInstant())
                .build();

        //TODO find the way to make possibile to set from abstract data
        AbstractBaseData abstractBaseData = mapper.mapRequest(baseData);

//        abstractBaseData.setId(event.getId());

        List<TableValue> tableValues = abstractBaseData.tableValues();
        tableValues.addAll(0, List.of(
                new TableValue("id", Types.VARCHAR, abstractBaseData.getId()),
                new TableValue("path", Types.VARCHAR, abstractBaseData.getPath()),
                new TableValue("method", Types.VARCHAR, abstractBaseData.getMethod()),
                new TableValue("timestamp", Types.TIMESTAMP, Timestamp.from(abstractBaseData.getTimestamp()))
        ));


        try {
            String tableName = "serve_" + HashGenerator.generateHash(mapper.tableName());

            dataDaoRepository.insert(tableName, tableValues);
        } catch (SQLException e) {
            log.error("Error inserting data: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating hash: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }


    @EventListener
    public void handleResponseEventData(EventDataResponse event) {

        log.info("Handling event: {}", event);

        BaseData baseData = BaseData.builder()
                .id(event.getId())
                .path(event.getPath())
                .responseBody(event.getResponse())
                .requestHeaders(event.getHeaders())
                .timestamp(event.getInstant())
                .build();

        //TODO find the way to make possibile to set from abstract data
        AbstractBaseData abstractBaseData = mapper.mapResponse(baseData);


        List<TableValue> tableValues = abstractBaseData.tableValues();
        tableValues.addAll(0, List.of(
                new TableValue("id", Types.VARCHAR, abstractBaseData.getId()),
                new TableValue("path", Types.VARCHAR, abstractBaseData.getPath()),
                new TableValue("timestamp", Types.TIMESTAMP, Timestamp.from(abstractBaseData.getTimestamp()))
        ));


        //TODO when I update data I need to get previous and merge with new one..only
        try {
            String tableName = "serve_" + HashGenerator.generateHash(mapper.tableName());

            Optional<Map<String, Object>> existingRowOptional = dataDaoRepository.findById(event.getId(), tableName);

            if (existingRowOptional.isPresent()) {
                // Step 2: Get the existing row and filter columns to update if they are currently null in the database
                Map<String, Object> existingRow = existingRowOptional.get();
                List<TableValue> valuesToUpdate = tableValues.stream()
                        .filter(tv -> existingRow.get(tv.key()) == null) // Only include columns with null values
                        .collect(Collectors.toList());

                if (!valuesToUpdate.isEmpty()) {
                    // Step 3: Update only columns with null values
                    dataDaoRepository.updateNullColumnsOnly(tableName, valuesToUpdate, event.getId());
                } else {
                    log.info("No null columns to update for ID: {}", event.getId());
                }
            } else {
                // If no row exists, insert a new one
                dataDaoRepository.insert(tableName, tableValues);
            }

        } catch (SQLException e) {
            log.error("Error inserting data: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating hash: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }








}
