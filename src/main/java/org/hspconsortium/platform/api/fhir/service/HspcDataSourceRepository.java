package org.hspconsortium.platform.api.fhir.service;

import org.hspconsortium.platform.api.fhir.MultiTenantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class HspcDataSourceRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(HspcDataSourceRepository.class);
    @Autowired
    private MultiTenantProperties multitenancyProperties;

    @Cacheable(cacheNames = "dataSource", key = "#tenantIdentifier", unless = "#result == null")
    public DataSource getDataSource(String tenantIdentifier) {
        DataSource dataSource = createDataSource(tenantIdentifier);
        if (dataSource != null) {
            LOGGER.info(String.format("Tenant '%s' maps to '%s' database url.", tenantIdentifier
                    , ((org.apache.tomcat.jdbc.pool.DataSource) dataSource).getPoolProperties().getUrl()));
        }
        return dataSource;
    }

    private DataSource createDataSource(String tenant) {
        final DataSourceProperties dataSourceProperties = this.multitenancyProperties.getDataSource(tenant);
        DataSourceBuilder factory = DataSourceBuilder
                .create(this.multitenancyProperties.getDb().getClassLoader())
                .driverClassName(this.multitenancyProperties.getDb().getDriverClassName())
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .url(dataSourceProperties.getUrl());

        DataSource dataSource = factory.build();
        Connection conn = null;
        try {
            //verify for a valid datasource
            conn = dataSource.getConnection();
            conn.isValid(2);
            conn.close(); // Return to connection pool
            conn = null;  // Make sure we don't close it twice

        } catch (SQLException e) {
            LOGGER.error(String.format("Connection couldn't be established for tenant '%s' with '%s' database url."
                    , tenant
                    , dataSourceProperties.getUrl()));
            dataSource = null;
        } finally {
            // Always make sure result sets and statements are closed, and the connection is returned to the pool
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    ;
                }
                conn = null;
            }
        }
        return dataSource;
    }
}
