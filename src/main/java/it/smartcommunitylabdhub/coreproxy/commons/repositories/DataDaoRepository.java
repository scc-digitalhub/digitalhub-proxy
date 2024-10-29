package it.smartcommunitylabdhub.coreproxy.commons.repositories;

import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableEntry;
import it.smartcommunitylabdhub.coreproxy.commons.interfaces.TableValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
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


    public void insert(String tableName, List<TableValue> tableValues) throws SQLException {

        if (tableValues.isEmpty()) {
            throw new IllegalArgumentException("No values to insert.");
        }

        String sql = "INSERT INTO " + tableName + "("
                + String.join(", ", tableValues.stream().map(e -> e.key()).toList()) +
                ") VALUES (" + String.join(", ", tableValues.stream().map(tableValue -> "?").toList()) + ");";


        int[] types = tableValues.stream()
                .map(TableValue::type)
                .mapToInt(Integer::intValue)
                .toArray();

        Object[] values = tableValues.stream()
                .map(TableValue::value)
                .toArray();

        jdbcTemplate.update(
                sql,
                values,
                types
        );

    }

    public void update(String tableName, List<TableValue> tableValues, String id) throws SQLException {
        // Check if tableValues is empty to avoid SQL syntax errors
        if (tableValues.isEmpty()) {
            throw new IllegalArgumentException("No values to update.");
        }

        String sql = "UPDATE " + tableName +
                " SET " + String.join(", ", tableValues.stream().map(tableValue -> tableValue.key() + " = ?").toList()) +
                " WHERE id = ?;";


        Object[] values = Stream.concat(
                tableValues.stream().map(TableValue::value),  // Stream of values
                Stream.of(id)  // Stream containing the id
        ).toArray();

        int[] types = Stream.concat(
                tableValues.stream().map(TableValue::type),  // Stream of types
                Stream.of(Types.VARCHAR)  // Stream containing the id type
        ).mapToInt(Integer::intValue).toArray();

        jdbcTemplate.update(
                sql,
                values,
                types
        );
    }


    public Optional<Map<String, Object>> findById(String id, String tableName) {
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(query, id);
            return Optional.of(result);
        } catch (Exception e) {
            // Handle case when no row is found or other database issues
            return Optional.empty();
        }
    }


    public void updateNullColumnsOnly(String tableName, List<TableValue> valuesToUpdate, String id) throws SQLException {
        if (valuesToUpdate.isEmpty()) {
            log.info("No values provided to update for ID: {}", id);
            return; // Nothing to update if there are no values provided
        }

        // Step 1: Query the current row for columns that are null
        String columnNames = valuesToUpdate.stream()
                .map(TableValue::key)
                .reduce((col1, col2) -> col1 + ", " + col2)
                .orElse(""); // Construct comma-separated list of column names

        String selectSql = "SELECT " + columnNames + " FROM " + tableName + " WHERE id = ?;";
        Map<String, Object> currentValues = jdbcTemplate.queryForMap(selectSql, id);

        // Step 2: Filter out columns that are already non-null in the database
        List<TableValue> nullColumnsToUpdate = valuesToUpdate.stream()
                .filter(tv -> currentValues.get(tv.key()) == null) // Only keep columns where DB value is null
                .toList();

        if (nullColumnsToUpdate.isEmpty()) {
            log.info("No null columns to update for ID: {}", id);
            return; // Nothing to update if there are no null columns
        }

        // Step 3: Build SQL query for updating only the null columns
        String updateSql = "UPDATE " + tableName +
                " SET " + String.join(", ", nullColumnsToUpdate.stream().map(tv -> tv.key() + " = ?").toList()) +
                " WHERE id = ?;";

        // Prepare the values and types arrays for the update operation
        Object[] values = Stream.concat(
                nullColumnsToUpdate.stream().map(TableValue::value),  // Stream of values to set in null columns
                Stream.of(id)  // Add the ID at the end
        ).toArray();

        int[] types = Stream.concat(
                nullColumnsToUpdate.stream().map(TableValue::type),  // Stream of column data types
                Stream.of(Types.VARCHAR)  // Add the ID type at the end
        ).mapToInt(Integer::intValue).toArray();

        // Step 4: Execute the update with values and types
        int rowsUpdated = jdbcTemplate.update(updateSql, values, types);
        log.info("Updated {} rows for ID: {}", rowsUpdated, id);
    }

}
