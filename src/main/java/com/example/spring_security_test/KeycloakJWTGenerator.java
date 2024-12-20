package com.example.spring_security_test;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class KeycloakJWTGenerator {
    public static void main(String[] args) {
        try {
            // Path to your private key in PEM format
            String privateKeyPath = "C:\\Users\\yasir.rehman\\spring-security-test\\private_key.pem"; // Replace with your actual private key path

            // Load the RSA Private Key
            RSAPrivateKey privateKey = loadRSAPrivateKey(privateKeyPath);

            // Create RSA-signer with the private key
            JWSSigner signer = new RSASSASigner(privateKey);

            // Define the JWT Claims
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .expirationTime(new Date(1734037754L * 1000)) // exp: 1734037754
                    .issueTime(new Date(1734001754L * 1000))      // iat: 1734001754
                    .jwtID("5d3fd7ce-65f2-4cc1-9416-d4051c669c75") // jti
                    .issuer("https://fhir.curemd.com:4334/realms/hapi-fhir-testing") // iss
                    .audience("account")                          // aud
                    .subject("9edb02ad-1dc8-4985-b8fb-15ecd30595d3") // sub
                    .claim("typ", "Bearer")
                    .claim("azp", "test")
                    .claim("session_state", "fe6ab360-0811-4182-a76e-ade07a8f037b")
                    .claim("acr", "1")
                    .claim("realm_access", createRealmAccess())
                    .claim("resource_access", createResourceAccess())
                    .claim("scope", "patient/Practitioner.read patient/CareTeam.read launch/patient patient/Observation.read patient/*.write patient/Medication.read fhirUser patient/Organization.read patient-attributes patient/Condition.read patient/Location.read patient/Immunization.read patient/Procedure.read patient/Encounter.read patient/Patient.read patient/MedicationRequest.read patient/Device.read patient/Goal.read patient/*.read profile patient/AllergyIntolerance.read patient/DiagnosticReport.read patient/Provenance.read patient/DocumentReference.read openid patient/CarePlan.read email")
                    .claim("sid", "fe6ab360-0811-4182-a76e-ade07a8f037b")
                    .claim("email_verified", true)
                    .claim("patient", Arrays.asList("123", "456", "789"))
                    .claim("name", "test user")
                    .claim("preferred_username", "test")
                    .claim("given_name", "test")
                    .claim("family_name", "user")
                    .claim("email", "test@gmail.com")
                    .build();

            // Define the JWT Header
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .keyID("lj3ChhEf8bbyiCsVVezov2n9KuU2-spm7bryirBQ18E") // kid
                    .build();

            // Create the Signed JWT
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            // Compute the RSA signature
            signedJWT.sign(signer);

            // Serialize the JWT to compact form
            String token = signedJWT.serialize();

            System.out.println("Generated JWT:");
            System.out.println(token);

            RSAPublicKey publicKey = loadRSAPublicKey("C:\\Users\\yasir.rehman\\spring-security-test\\rs256_public_key.pem");

            boolean isValid = verifyJWT(token, publicKey);
            System.out.println("Is the JWT signature valid? {}" +  isValid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads an RSA Private Key from a PEM file.
     *
     * @param filepath Path to the PEM file containing the private key.
     * @return RSAPrivateKey instance.
     * @throws Exception If an error occurs during key loading.
     */
    private static RSAPrivateKey loadRSAPrivateKey(String filepath) throws Exception {
        // Read all bytes from the PEM file
        String pem = new String(Files.readAllBytes(Paths.get(filepath)));

        // Remove the PEM headers and newlines
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // Decode the Base64 content
        byte[] der = Base64.getDecoder().decode(pem);

        // Generate the private key
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    /**
     * Creates the 'realm_access' claim.
     *
     * @return Map representing realm_access.
     */
    private static Map<String, Object> createRealmAccess() {
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList(
                "manager",
                "default-roles-hapi-fhir-testing",
                "offline_access",
                "uma_authorization"
        ));
        return realmAccess;
    }

    /**
     * Creates the 'resource_access' claim.
     *
     * @return Map representing resource_access.
     */
    private static Map<String, Object> createResourceAccess() {
        Map<String, Object> accountAccess = new HashMap<>();
        accountAccess.put("roles", Arrays.asList(
                "manage-account",
                "manage-account-links",
                "view-profile"
        ));

        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("account", accountAccess);
        return resourceAccess;
    }

    public static boolean verifyJWT(String token, RSAPublicKey publicKey) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            return signedJWT.verify(verifier);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static RSAPublicKey loadRSAPublicKey(String filepath) throws Exception {
        String pem = new String(Files.readAllBytes(Paths.get(filepath)));
        pem = pem.replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

}
