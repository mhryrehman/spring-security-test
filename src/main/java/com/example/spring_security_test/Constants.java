package com.example.spring_security_test;

import java.util.Arrays;
import java.util.List;

public class Constants {
	public static final List<String> PUBLIC_URIS = Arrays.asList("/fhir/metadata", "/fhir/Composition", "/fhir/Parameters", "/fhir/Binary", "/metadata/.well-known/smart-configuration");
	public static final String FHIR_URI = "/fhir/";
	public static final String FHIR_SCOPES_CLAIM = "scope";
	public static final String TENANT_ID_CLAIM = "tenantId";
	public static final String REALM_ACCESS_CLAIM = "realm_access";
	public static final String ROLES_CLAIM = "roles";
	public static final String CLIENT_ID_CLAIM = "client_id";
	public static final String DEFAULT_PARTITION = "DEFAULT";
	public static final String ADMIN_ROLE = "admin";
	public static final String UNAUTHORIZED_TOKEN_MESSAGE = "Unauthorized: Token not present or invalid.";
	public static final String INVALID_TENANT_ID_MESSAGE = "Unauthorized: Invalid tenant ID.";
	public static final String INVALID_URI_MESSAGE = "Access Denied: Invalid URI.";
	public static final String ADMIN_PRIVILEGE_REQUIRED_MESSAGE = "Access Denied: Admin privilege required.";
	public static final String REQUIRED_SCOPE_NOT_FOUND_MESSAGE = "Access Denied: Required scope not found in token.";
}
