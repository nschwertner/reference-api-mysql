package org.hspconsortium.platform.api.fhir;

import org.hspconsortium.platform.api.fhir.interceptor.MultiTenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MultiTenantWebMvcConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new MultiTenantInterceptor());
	}
}
