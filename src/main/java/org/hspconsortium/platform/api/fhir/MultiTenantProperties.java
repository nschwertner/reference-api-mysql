package org.hspconsortium.platform.api.fhir;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("hspc.platform.api.fhir")
public class MultiTenantProperties {
	public static final String DEFAULT_TENANT_ID = "hspc";
	public static final String CURRENT_TENANT_IDENTIFIER = "current_tenant_identifier";

	@NestedConfigurationProperty
	private DataSourceProperties db;

	public DataSourceProperties getDb() {
		return db;
	}

	public void setDb(DataSourceProperties db) {
		this.db = db;
	}

	public DataSourceProperties getDataSource(String tenant) {
		DataSourceProperties dataSourceProperties = new DataSourceProperties();
		final String schema = "hapi_" + tenant;
		String url = db.getUrl().replace(db.getSchema(), schema);
		dataSourceProperties.setUrl(url);
		dataSourceProperties.setUsername(db.getUsername());
		dataSourceProperties.setPassword(db.getPassword());
		dataSourceProperties.setSchema(schema);
		dataSourceProperties.setData((db.getData()));
		dataSourceProperties.setBeanClassLoader(db.getClassLoader());
		dataSourceProperties.setDriverClassName(db.getDriverClassName());
		dataSourceProperties.setPlatform(db.getPlatform());
		return dataSourceProperties;
	}
}