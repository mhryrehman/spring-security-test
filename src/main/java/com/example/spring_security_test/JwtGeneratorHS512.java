package com.example.spring_security_test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtGeneratorHS512 {

    public static void main(String[] args) {
        // Base64-encoded HS512 secret key
        String base64Key = "4Fhw5k7N9h0pqZCzQk3WV0EXNQoFI7eIcm7frNVkMuAPmd3I5FHnzxKw7Pv4kNJXX3c2Z9sp2dxUwRvfncIVAA==";

        // Decode the key
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        Key signingKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA512");

        // Set the expiration and issued-at times
        long nowMillis = System.currentTimeMillis();
        Date issuedAt = new Date(nowMillis);
        Date expiration = new Date(nowMillis + (3600 * 1000 * 10)); // 10 hours (adjust as needed)

        // Prepare claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", "9580ceff-2fdf-4323-a0e6-bff95eeae5e9");
        claims.put("iss", "http://172.16.102.238:8082/realms/testing");
        claims.put("aud", List.of("realm-management", "broker", "account"));
        claims.put("sub", "8ee1e986-58d9-4002-b0aa-6bd388c35b72");
        claims.put("typ", "Bearer");
        claims.put("azp", "test");
        claims.put("sid", "f5d14eaa-efb6-4d15-9a12-5b9de563c88f");
        claims.put("acr", "1");
        claims.put("exp", 1734363767L); // Expiration time
        claims.put("iat", 1734327767L); // Issued at time

        // Add realm_access
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("default-roles-testing", "offline_access", "uma_authorization"));
        claims.put("realm_access", realmAccess);

        // Add resource_access
        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("realm-management", Map.of("roles", List.of(
                "view-identity-providers", "view-realm", "manage-identity-providers", "impersonation", "realm-admin",
                "create-client", "manage-users", "query-realms", "view-authorization", "query-clients", "query-users",
                "manage-events", "manage-realm", "view-events", "view-users", "view-clients", "manage-authorization",
                "manage-clients", "query-groups")));
        resourceAccess.put("test", Map.of("roles", List.of("uma_protection")));
        resourceAccess.put("broker", Map.of("roles", List.of("read-token")));
        resourceAccess.put("account", Map.of("roles", List.of(
                "manage-account", "view-applications", "view-consent", "view-groups", "manage-account-links",
                "manage-consent", "delete-account", "view-profile")));
        claims.put("resource_access", resourceAccess);

        // Additional claims
        claims.put("scope", "openid email profile");
        claims.put("email_verified", true);
        claims.put("name", "admin admin");
        claims.put("preferred_username", "admin");
        claims.put("given_name", "admin");
        claims.put("family_name", "admin");
        claims.put("email", "admin@gmail.com");

        // Generate the token
        String jwt = Jwts.builder()
                .setHeaderParam("alg", "HS512")
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("kid", "30327d61-1f71-45d0-9a51-a2f8f45f9337")
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .setClaims(claims)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();

        // Print the generated token
        System.out.println("Generated JWT:");
        System.out.println(jwt);
    }
}
