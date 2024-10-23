package it.smartcommunitylabdhub.coreproxy.commons.listeners;


import it.smartcommunitylabdhub.coreproxy.commons.events.ResponseEventData;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.MapperModule;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableEntry;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;
import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;
import it.smartcommunitylabdhub.coreproxy.commons.repositories.DataDaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;


@Component
@Slf4j
public class DataEventListener implements InitializingBean {


    private final MapperModule mapper;
    private final DataDaoRepository dataDaoRepository;

    @Autowired
    public DataEventListener(@Value("${mapper.module}") String qualifier,
                             ApplicationContext applicationContext, DataDaoRepository dataDaoRepository) {

        this.mapper = (MapperModule) applicationContext.getBean(qualifier);
        this.dataDaoRepository = dataDaoRepository;
        if (this.mapper == null) {
            throw new IllegalArgumentException("No mapper found with qualifier: " + qualifier);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //TODO: create table on db.
        List<TableEntry> tableEntries = mapper.tableEntries();
        tableEntries.addAll(0, List.of(
                new TableEntry("id", "VARCHAR(30)"),
                new TableEntry("path", "VARCHAR(255)"),
                new TableEntry("timestamp", "TIMESTAMP")
        ));

        dataDaoRepository.createTable("serve_result", tableEntries);

    }


    @EventListener
    public void handleRequestEventData(ResponseEventData event) {

        log.info("Handling event: {}", event);


        AbstractBaseData abstractBaseData = mapper.map(event.getBaseData());
        // TODO: GET DATA FROM HEADER
        // abstractBaseData.setTimestamp(Instant.parse(event.getHeaders().get("date")));


        List<TableValue> tableValues = abstractBaseData.tableValues();
        tableValues.addAll(0, List.of(
                new TableValue("id", Types.VARCHAR, abstractBaseData.getId()),
                new TableValue("path", Types.VARCHAR, abstractBaseData.getPath()),
                new TableValue("timestamp", Types.TIMESTAMP, abstractBaseData.getRequestTimestamp())
        ));

//        try {
//            dataDaoRepository.insert("serve_result", tableValues);
//        } catch (SQLException e) {
//            log.error("Error inserting data: {}", e.getMessage());
//        }


//        dataDaoRepository.insert();
//
//        Class<? extends AbstractBaseData> targetType = event.getTargetType();

//
//        DataBufferConverter<T> converter = (DataBufferConverter<T>) converterMap.get(targetType);
//
//        if (converter != null) {
//            converter.convert(dataBuffer).subscribe(convertedItem -> {
//
//                try {
//                    dynamicDaoService.create(convertedItem);
//                } catch (SQLException e) {
//                    throw new RuntimeException(e);
//                }
//                log.info("Converted item of type {}: {}",
//                        convertedItem.getClass().getSimpleName(),
//                        convertedItem);
//            });


//        } else {
//            log.warn("No suitable converter found for target type: {}", targetType);
//        }
    }


    @EventListener
    public void handleResponseEventData(ResponseEventData event) {

        log.info("Handling event: {}", event);


        AbstractBaseData abstractBaseData = mapper.map(event.getBaseData());
        // TODO: GET DATA FROM HEADER
       // abstractBaseData.setTimestamp(Instant.parse(event.getHeaders().get("date")));


        List<TableValue> tableValues = abstractBaseData.tableValues();
        tableValues.addAll(0, List.of(
                new TableValue("id", Types.VARCHAR, abstractBaseData.getId()),
                new TableValue("path", Types.VARCHAR, abstractBaseData.getPath()),
                new TableValue("timestamp", Types.TIMESTAMP, abstractBaseData.getRequestTimestamp())
        ));

//        try {
//            dataDaoRepository.insert("serve_result", tableValues);
//        } catch (SQLException e) {
//            log.error("Error inserting data: {}", e.getMessage());
//        }


//        dataDaoRepository.insert();
//
//        Class<? extends AbstractBaseData> targetType = event.getTargetType();

//
//        DataBufferConverter<T> converter = (DataBufferConverter<T>) converterMap.get(targetType);
//
//        if (converter != null) {
//            converter.convert(dataBuffer).subscribe(convertedItem -> {
//
//                try {
//                    dynamicDaoService.create(convertedItem);
//                } catch (SQLException e) {
//                    throw new RuntimeException(e);
//                }
//                log.info("Converted item of type {}: {}",
//                        convertedItem.getClass().getSimpleName(),
//                        convertedItem);
//            });


//        } else {
//            log.warn("No suitable converter found for target type: {}", targetType);
//        }
    }

}
