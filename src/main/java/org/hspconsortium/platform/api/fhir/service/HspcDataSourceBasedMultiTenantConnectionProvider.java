package org.hspconsortium.platform.api.fhir.service;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hspconsortium.platform.api.fhir.MultiTenantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Component
public class HspcDataSourceBasedMultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(HspcDataSourceBasedMultiTenantConnectionProvider.class);

    @Autowired
    private HspcDataSourceRepository hspcDataSourceRepository;

    @Autowired
    private DataSource defaultDataSource;

    @PostConstruct
    public void load() {
        //some initwork();
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return defaultDataSource;
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        if ((tenantIdentifier == null) || (MultiTenantProperties.DEFAULT_TENANT_ID.equals(tenantIdentifier))) {
//            return this.defaultDataSource;
            return hspcDataSourceRepository.getDataSource(MultiTenantProperties.DEFAULT_TENANT_SANDBOX_ID);
        }
        return hspcDataSourceRepository.getDataSource(tenantIdentifier);
    }

}
