package org.hspconsortium.platform.api.fhir.interceptor;

import org.hspconsortium.platform.api.fhir.MultiTenantProperties;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class MultiTenantInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
			throws Exception {
		Map<String, Object> pathVars = (Map<String, Object>) req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		
		if (pathVars.containsKey("tenant")) {
			req.setAttribute(MultiTenantProperties.CURRENT_TENANT_IDENTIFIER, pathVars.get("tenant"));
		}
		return true;
	}
}
