package org.hspconsortium.platform.api.fhir;

import ca.uhn.fhir.jpa.config.BaseJavaConfigDstu2;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.util.SubscriptionsRequireManualActivationInterceptorDstu2;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement()
public class MySQLConfig extends BaseJavaConfigDstu2 {

    @Value("${hspc.platform.api.fhir.db.driver}")
    private String databaseDriver;

    @Value("${hspc.platform.api.fhir.db.url}")
    private String databaseUrl;

    @Value("${hspc.platform.api.fhir.db.username}")
    private String databaseUsername;

    @Value("${hspc.platform.api.fhir.db.password}")
    private String databasePassword;

    @Value("${hspc.platform.api.fhir.db.persistenceUnitName}")
    private String persistenceUnitName;

	@Value("${hspc.platform.api.fhir.hibernate.dialect}")
	private String hibernateDialect;

	@Value("${hibernate.search.default.indexBase}")
	private String luceneBase;

	/**
	 * Configure FHIR properties around the the JPA server via this bean
	 */
	@Bean()
	public DaoConfig daoConfig() {
		DaoConfig retVal = new DaoConfig();
		retVal.setSubscriptionEnabled(true);
		retVal.setSubscriptionPollDelay(5000);
		retVal.setSubscriptionPurgeInactiveAfterMillis(DateUtils.MILLIS_PER_HOUR);
		retVal.setAllowMultipleDelete(true);
		return retVal;
	}

    @Bean
	public DataSource dataSource() {
		DriverManagerDataSource retVal = new DriverManagerDataSource();
		retVal.setDriverClassName(databaseDriver);
		retVal.setUrl(databaseUrl);
		retVal.setUsername(databaseUsername);
		retVal.setPassword(databasePassword);
		return retVal;
	}

	@Bean()
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean retVal = new LocalContainerEntityManagerFactoryBean();
		retVal.setPersistenceUnitName(persistenceUnitName);
		retVal.setDataSource(dataSource());
		retVal.setPackagesToScan("ca.uhn.fhir.jpa.entity");
		retVal.setPersistenceProvider(new HibernatePersistenceProvider());
		retVal.setJpaProperties(jpaProperties());
		retVal.afterPropertiesSet();
		return retVal;
	}

	private Properties jpaProperties() {
		Properties extraProperties = new Properties();
		extraProperties.put("hibernate.dialect", hibernateDialect);
		extraProperties.put("hibernate.format_sql", "true");
		extraProperties.put("hibernate.show_sql", "false");
		extraProperties.put("hibernate.hbm2ddl.auto", "update");
		extraProperties.put("hibernate.jdbc.batch_size", "20");
		extraProperties.put("hibernate.cache.use_query_cache", "false");
		extraProperties.put("hibernate.cache.use_second_level_cache", "false");
		extraProperties.put("hibernate.cache.use_structured_entries", "false");
		extraProperties.put("hibernate.cache.use_minimal_puts", "false");
		extraProperties.put("hibernate.search.default.directory_provider", "filesystem");
		extraProperties.put("hibernate.search.default.indexBase", luceneBase);
		extraProperties.put("hibernate.search.lucene_version", "LUCENE_CURRENT");
		return extraProperties;
	}

	/**
	 * Do some fancy logging to create a nice access log that has details about each incoming request.
	 */
	public IServerInterceptor loggingInterceptor() {
		LoggingInterceptor retVal = new LoggingInterceptor();
		retVal.setLoggerName("fhirtest.access");
		retVal.setMessageFormat(
				"Path[${servletPath}] Source[${requestHeader.x-forwarded-for}] Operation[${operationType} ${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] ResponseEncoding[${responseEncodingNoDefault}]");
		retVal.setLogExceptions(true);
		retVal.setErrorMessageFormat("ERROR - ${requestVerb} ${requestUrl}");
		return retVal;
	}

	/**
	 * This interceptor adds some pretty syntax highlighting in responses when a browser is detected
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	public IServerInterceptor responseHighlighterInterceptor() {
		ResponseHighlighterInterceptor retVal = new ResponseHighlighterInterceptor();
		return retVal;
	}

	@Bean(autowire = Autowire.BY_TYPE)
	public IServerInterceptor subscriptionSecurityInterceptor() {
		return new SubscriptionsRequireManualActivationInterceptorDstu2();
	}

	@Bean()
	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager retVal = new JpaTransactionManager();
		retVal.setEntityManagerFactory(entityManagerFactory);
		return retVal;
	}

}
