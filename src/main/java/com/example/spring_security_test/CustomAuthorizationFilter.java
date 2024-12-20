package com.example.spring_security_test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String requestUri = request.getRequestURI();
		System.out.println("Custom authorization filter invoked for URI: " + requestUri);

		// If the URI is public, continue the request without further checks
		if (Constants.PUBLIC_URIS.contains(requestUri)) {
			filterChain.doFilter(request, response);
			return;
		}

		// Retrieve the JWT token from the SecurityContext
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, Constants.UNAUTHORIZED_TOKEN_MESSAGE);
			return;
		}

		String partitionName = extractPartitionName(requestUri);
		List<String> roles = extractRolesFromJwt(jwt);
		String requiredScope = getRequiredScope(request);

		if (roles.contains(Constants.ADMIN_ROLE)) {
			filterChain.doFilter(request, response);
			return;
		}
		// Check if partition name in URI is DEFAULT or matches with the tenantId in the JWT claims
		if (!isPartitionValid(partitionName, jwt)) {
			sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, Constants.INVALID_TENANT_ID_MESSAGE);
			return;
		}

		if (requiredScope.isEmpty()) {
			sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, Constants.INVALID_URI_MESSAGE);
			return;
		}

		if (requiredScope.startsWith("$") && !roles.contains(Constants.ADMIN_ROLE)) {
			sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, Constants.ADMIN_PRIVILEGE_REQUIRED_MESSAGE);
			return;
		}


		//Validate Scopes only for client token.
		if (StringUtils.hasText(jwt.getClaim(Constants.CLIENT_ID_CLAIM))) {
			// Retrieve and validate required scopes from JWT and request header
			List<String> jwtScopes = extractFhirScopes(jwt);
			if (!jwtScopes.contains("*.*") && !isScopeExistInTokenScopes(jwtScopes, request)) {
				sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, Constants.REQUIRED_SCOPE_NOT_FOUND_MESSAGE);
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	/**
	 * Extracts FHIR scopes from the JWT token.
	 *
	 * @param jwt the JWT token
	 * @return a list of FHIR scopes
	 */
	private List<String> extractFhirScopes(Jwt jwt) {
		if (jwt == null) return Collections.emptyList();

		String fhirScopesClaim = jwt.getClaim(Constants.FHIR_SCOPES_CLAIM);
		return StringUtils.hasText(fhirScopesClaim) ?
			Arrays.stream(fhirScopesClaim.split(" "))
				.map(item -> {
					String[] segments = item.split("/");
					return segments[segments.length - 1];
				})
				.toList()
			: Collections.emptyList();
	}

	/**
	 * Extracts the partition name (the first segment after /fhir/) from the URI.
	 *
	 * @param uri the request URI
	 * @return the partition name, or null if not found
	 */
	private String extractPartitionName(String uri) {
		int fhirIndex = uri.indexOf(Constants.FHIR_URI);
		if (fhirIndex != -1) {
			String pathAfterFhir = uri.substring(fhirIndex + Constants.FHIR_URI.length());
			String[] segments = pathAfterFhir.split("/");
			return segments.length > 0 ? segments[0] : null;
		}
		return null;
	}

	/**
	 * Retrieves the required scope from the request URI.
	 *
	 * @param request the HTTP servlet request
	 * @return the required scope as a string
	 */
	private String getRequiredScope(HttpServletRequest request) {
		String[] segments = request.getRequestURI().split("/");
		return segments.length > 3 ? segments[3].split("\\?")[0] : "";
	}

	/**
	 * Validates if the required scope exists in the token's scopes.
	 *
	 * @param jwtScopes the list of JWT scopes
	 * @param request   the HTTP servlet request
	 * @return true if the scope exists, otherwise false
	 */
	private boolean isScopeExistInTokenScopes(List<String> jwtScopes, HttpServletRequest request) {
		String operation = switch (request.getMethod()) {
			case "GET" -> "read";
			case "POST", "PUT" -> "write";
			default -> "";
		};

		String requiredScope = getRequiredScope(request);
		if (!StringUtils.hasText(requiredScope)) {
			System.out.println("Required scope is empty or invalid.");
			return false;
		}

		return jwtScopes.stream().anyMatch(scope -> {
			String[] segments = scope.split("\\.");
			return segments.length == 2
				&& ("*".equals(segments[0]) || requiredScope.equals(segments[0]))
				&& ("*".equals(segments[1]) || operation.equals(segments[1]));
		});
	}

	/**
	 * Extracts roles from the "realm_access" claim in the JWT token.
	 *
	 * @param jwt the JWT token
	 * @return a list of roles, or an empty list if none found
	 */
	private List<String> extractRolesFromJwt(Jwt jwt) {
		Map<String, Object> realmAccess = jwt.getClaim(Constants.REALM_ACCESS_CLAIM);
		if (realmAccess != null && realmAccess.containsKey(Constants.ROLES_CLAIM)) {
			return (List<String>) realmAccess.get(Constants.ROLES_CLAIM);
		}
		return Collections.emptyList();
	}

	/**
	 * Validates if the partition in the URI matches the tenant ID in the JWT token.
	 *
	 * @param partitionName the partition name from the URI
	 * @param jwt           the JWT token
	 * @return true if valid, otherwise false
	 */
	private boolean isPartitionValid(String partitionName, Jwt jwt) {
		String tenantId = jwt.getClaim(Constants.TENANT_ID_CLAIM);
		return StringUtils.hasText(partitionName) &&
			(Constants.DEFAULT_PARTITION.equals(partitionName) || Objects.equals(partitionName, tenantId));
	}

	/**
	 * Sends an error response with a given status code and message.
	 *
	 * @param response   the HTTP servlet response
	 * @param statusCode the status code to set
	 * @param message    the error message to include
	 * @throws IOException if an input or output error occurs
	 */
	private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
		response.setStatus(statusCode);
		response.setContentType("application/json"); // Set content type to JSON
		response.setCharacterEncoding("UTF-8"); // Ensure UTF-8 encoding

		// Create a JSON-formatted error message
		String jsonResponse = String.format("{\"error\": \"%s\"}", message);

		response.getWriter().write(jsonResponse);
		response.getWriter().flush();
		response.getWriter().close();
		System.err.println(message);
	}
}
