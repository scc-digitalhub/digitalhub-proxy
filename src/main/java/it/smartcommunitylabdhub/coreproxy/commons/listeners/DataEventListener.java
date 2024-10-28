package it.smartcommunitylabdhub.coreproxy.commons.listeners;


import it.smartcommunitylabdhub.coreproxy.commons.events.EventData;
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
import java.util.Optional;


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
                new TableEntry("timestamp", "TIMESTAMP")
        ));

        dataDaoRepository.createTable("serve_" + HashGenerator.generateHash(mapper.tableName()), tableEntries);

    }


    @EventListener
    public void handleRequestEventData(EventData event) {

        log.info("Handling event: {}", event);

        BaseData baseData = BaseData.builder()
                .id(event.getId())
                .path(event.getPath())
                .responseBody(event.getResponse())
                .requestBody(event.getRequest())
                .requestHeaders(event.getHeaders())
                .timestamp(event.getRequestTimestamp())
                .build();

        //TODO find the way to make possibile to set from abstract data
        AbstractBaseData abstractBaseData = mapper.mapRequest(baseData);

//        abstractBaseData.setId(event.getId());

        List<TableValue> tableValues = abstractBaseData.tableValues();
        tableValues.addAll(0, List.of(
                new TableValue("id", Types.VARCHAR, abstractBaseData.getId()),
                new TableValue("path", Types.VARCHAR, abstractBaseData.getPath()),
                new TableValue("timestamp", Types.TIMESTAMP, Timestamp.from(abstractBaseData.getTimestamp()))
        ));


        try {
            dataDaoRepository.insert("serve_" + HashGenerator.generateHash(mapper.tableName()), tableValues);
        } catch (SQLException e) {
            log.error("Error inserting data: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating hash: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }


    @EventListener
    public void handleResponseEventData(EventData event) {

        log.info("Handling event: {}", event);

        BaseData baseData = BaseData.builder()
                .id(event.getId())
                .path(event.getPath())
                .responseBody(event.getResponse())
                .requestBody(event.getRequest())
                .requestHeaders(event.getHeaders())
                .timestamp(event.getRequestTimestamp())
                .build();

        //TODO find the way to make possibile to set from abstract data
        AbstractBaseData abstractBaseData = mapper.mapResponse(baseData);


        List<TableValue> tableValues = abstractBaseData.tableValues();
        tableValues.addAll(0, List.of(
                new TableValue("id", Types.VARCHAR, abstractBaseData.getId()),
                new TableValue("path", Types.VARCHAR, abstractBaseData.getPath()),
                new TableValue("timestamp", Types.TIMESTAMP, Timestamp.from(abstractBaseData.getTimestamp()))
        ));





        try {
            String tableName = "serve_" + HashGenerator.generateHash(mapper.tableName());

            dataDaoRepository.update("serve_" + HashGenerator.generateHash(mapper.tableName()), tableValues, event.getId());
        } catch (SQLException e) {
            log.error("Error inserting data: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating hash: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
