package it.smartcommunitylabdhub.coreproxy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

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
