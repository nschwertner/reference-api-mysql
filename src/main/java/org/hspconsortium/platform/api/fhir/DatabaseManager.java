package org.hspconsortium.platform.api.fhir;

import org.hspconsortium.platform.api.fhir.util.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

@Component
@PropertySource("classpath:/config/mysql.properties")
public class DatabaseManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);

    // need a connection that isn't tied to a schema
    @Autowired
    private DataSource noSchemaDataSource;

    public Collection<String> getSchemas(String schemaPrefix) {
        Collection<String> results = new ArrayList<>();

        try {
            Connection connection = noSchemaDataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format("SHOW DATABASES LIKE '%s_%s'", schemaPrefix, "%"));
            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding schemas", e);
        }
    }

    public boolean createSchema(String schema) {
        LOGGER.info("Creating schema: " + schema);
        Connection connection = null;
        try {
            connection = noSchemaDataSource.getConnection();
            Statement statement = connection.createStatement();
            int result = statement.executeUpdate("CREATE DATABASE " + schema);
            LOGGER.info("Creating schema result: " + result);
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating schema: " + schema, e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                    connection = null;
                }
            } catch (SQLException e) {
                LOGGER.error("Error closing connection", e);
            }
        }
    }

    public boolean loadInitialDataset(String schema, Reader reader) {
        LOGGER.info("Loading Initial Dataset for: " + schema);
        Connection connection = null;
        try {
            connection = noSchemaDataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("USE " + schema);
            ScriptRunner scriptRunner = new ScriptRunner(connection, true, true, true);
            scriptRunner.runScript(reader);
            return true;
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error loading initial dataset for schema: " + schema, e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                    connection = null;
                }
            } catch (SQLException e) {
                LOGGER.error("Error closing connection", e);
            }
        }
    }

    public boolean dropSchema(String schema) {
        LOGGER.info("Dropping schema: " + schema);
        Connection connection = null;
        try {
            connection = noSchemaDataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("DROP DATABASE IF EXISTS " + schema);
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error dropping schema: " + schema, e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                    connection = null;
                }
            } catch (SQLException e) {
                LOGGER.error("Error closing connection", e);
            }
        }
    }
}
