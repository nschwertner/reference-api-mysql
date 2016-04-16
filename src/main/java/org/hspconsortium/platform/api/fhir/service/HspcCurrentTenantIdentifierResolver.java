package org.hspconsortium.platform.api.fhir.service;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hspconsortium.platform.api.fhir.MultiTenantProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class HspcCurrentTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

	@Override
	public String resolveCurrentTenantIdentifier() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

		if (requestAttributes != null) {
			String identifier = (String) requestAttributes.getAttribute(MultiTenantProperties.CURRENT_TENANT_IDENTIFIER, RequestAttributes.SCOPE_REQUEST);
			if (identifier != null) {
				return identifier;
			}
		}
		return MultiTenantProperties.DEFAULT_TENANT_ID;
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}
}
