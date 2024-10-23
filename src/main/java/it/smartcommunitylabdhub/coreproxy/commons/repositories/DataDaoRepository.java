package it.smartcommunitylabdhub.coreproxy.commons.repositories;

import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableEntry;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DataDaoRepository {

    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public DataDaoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    //TODO : add default entries to tableEntries before call this method
    public void createTable(String tableName, List<TableEntry> tableEntries) {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                "(" + String.join(", ", tableEntries.stream().map(tableEntry -> tableEntry.key() + " " + tableEntry.type()).toList()) + ");";

        jdbcTemplate.execute(sql);

        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    //TODO: Add default entries to tableValues before call this method
    public void insert(String tableName, List<TableValue> tableValues) throws SQLException {
        String sql = "INSERT INTO TABLE " + tableName + "("
                + String.join(", ", tableValues.stream().map(e -> e.key()).toList()) +
                ") VALUES (" + String.join(", ", tableValues.stream().map(tableValue -> "?").toList()) + ");";


        jdbcTemplate.update(
                sql,
                tableValues.stream().map(tableValue -> tableValue.value()).toArray(),
                tableValues.stream().map(tableValue -> tableValue.type()).toArray()
        );

    }
}
