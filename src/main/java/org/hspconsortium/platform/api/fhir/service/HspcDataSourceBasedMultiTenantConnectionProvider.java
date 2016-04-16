package org.hspconsortium.platform.api.fhir.service;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hspconsortium.platform.api.fhir.MultiTenantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class HspcDataSourceBasedMultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(HspcDataSourceBasedMultiTenantConnectionProvider.class);
    private static final String DEFAULT_TENANT_ID = "hspc";
    private Map<String, DataSource> tanentDataSourceMap;

    @Autowired
    private MultiTenantProperties multitenancyProperties;

    @Autowired
    private DataSource defaultDataSource;

    @PostConstruct
    public void load() {
        tanentDataSourceMap = new HashMap<>();
        tanentDataSourceMap.put("hspc", defaultDataSource);
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return tanentDataSourceMap.get(DEFAULT_TENANT_ID);
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        DataSource dataSource = tanentDataSourceMap.get(tenantIdentifier);
        if (dataSource == null) {
            dataSource = createDataSource(tenantIdentifier);
            if (dataSource != null)
                tanentDataSourceMap.put(tenantIdentifier, dataSource);

        }
        LOGGER.info(String.format("Tenant '%s' is pointing to '%s' database url.", tenantIdentifier
                , ((org.apache.tomcat.jdbc.pool.DataSource) dataSource).getPoolProperties().getUrl()));
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
        try {
            dataSource.getConnection().prepareCall(dataSourceProperties.getData()).execute();
        } catch (SQLException e) {
            LOGGER.warn(String.format("Connection couldn't be established for tenant '%s' with '%s' database url."
                    , tenant
                    , dataSourceProperties.getUrl()));
            dataSource = null;
        }
        return dataSource;
    }
}
