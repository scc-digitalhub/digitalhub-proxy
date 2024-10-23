package it.smartcommunitylabdhub.coreproxy.config;
import it.smartcommunitylabdhub.coreproxy.commons.mappers.BaseDataMapper;
import it.smartcommunitylabdhub.coreproxy.commons.models.AbstractBaseData;
import it.smartcommunitylabdhub.coreproxy.commons.repositories.DataDaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataDaoConfiguration {

    private final DataSource dataSource;
    private final ApplicationContext applicationContext;

    @Autowired
    public DataDaoConfiguration(DataSource dataSource,
                                ApplicationContext applicationContext) {
        this.dataSource = dataSource;
        this.applicationContext = applicationContext;
    }
}
