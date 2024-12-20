package com.example.spring_security_test;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Base64;

public class JWKToPEMPublicKeyConverter {

    public static void main(String[] args) throws ParseException {
        try {
            // Your JWKS JSON as a String
            String jwkSetJson = "{\n" +
                    "    \"keys\": [\n" +
                    "        {\n" +
                    "            \"kid\": \"lj3ChhEf8bbyiCsVVezov2n9KuU2-spm7bryirBQ18E\",\n" +
                    "            \"kty\": \"RSA\",\n" +
                    "            \"alg\": \"RS256\",\n" +
                    "            \"use\": \"sig\",\n" +
                    "            \"n\": \"2Sm2KUP1h3s0UWdTZ-b-WV-_CXqV68NRzaF3i2fZ7C50cAI6YdZ4cmS6kg5VKKAX8DxH6cA2lKT_4sY3S8ZX-jTEH4zMTPHAcFJrzbU9KSiJwKHw43H0BkMfkZtx2ru-Dgvlt2yMSv9pGsMYdeLtefjEty7VevvBd5SLdOd6LL1_vxIOOW-v6SpQi9RAsWCnfuPuBct2XdK6svjv_FdhE63vC14sl-oLYUjCcJAEImqyRhuFzXl26JbLQeENTXmmAoe_J0zzV8wIqpt0lOv2z2Maj5gAUtqSnmPILsGObvqLZiyfI93Ku71KcfIiAijpa5LLktVRAvjHv74NE_8XPw\",\n" +
                    "            \"e\": \"AQAB\",\n" +
                    "            \"x5c\": [\n" +
                    "                \"MIICsTCCAZkCBgGTPgKYLjANBgkqhkiG9w0BAQsFADAcMRowGAYDVQQDDBFoYXBpLWZoaXItdGVzdGluZzAeFw0yNDExMTgwNjQxMTdaFw0zNDExMTgwNjQyNTdaMBwxGjAYBgNVBAMMEWhhcGktZmhpci10ZXN0aW5nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2Sm2KUP1h3s0UWdTZ+b+WV+/CXqV68NRzaF3i2fZ7C50cAI6YdZ4cmS6kg5VKKAX8DxH6cA2lKT/4sY3S8ZX+jTEH4zMTPHAcFJrzbU9KSiJwKHw43H0BkMfkZtx2ru+Dgvlt2yMSv9pGsMYdeLtefjEty7VevvBd5SLdOd6LL1/vxIOOW+v6SpQi9RAsWCnfuPuBct2XdK6svjv/FdhE63vC14sl+oLYUjCcJAEImqyRhuFzXl26JbLQeENTXmmAoe_J0zzV8wIqpt0lOv2z2Maj5gAUtqSnmPILsGObvqLZiyfI93Ku71KcfIiAijpa5LLktVRAvjHv74NE/8XPwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQAlrmrFY7NOi17qVmyuuzpedvw+xcoDAiJYn2mOb7z+9rSIftsxdPMtsVO8w+LfBUc1iQeo/fWx1h96JXLzG6wYH7ChQZzpTQ7yc7+Te/+m7VtIr7JlbXJZEWBA8/igZZjM589bt0mW5Fk0143a6xt1/dklTdpQSFztm+lt0O+u6xUyHfjZry/5NB5pkrQxDisAUeDa+Ee4Fw0FDIZ1Y+HnY2rnZdJBmHfnM5COX+IYuuQY8qMcE/uCKOH1yYQqLYXlDCY7u90Qbkezd8+Kn3S+JgaW2j/tOuIN5ay9x3I/k0qoQghapK2YPXMquKAOZxH6Tscc/c9ZpReik+TK1/wf\"\n" +
                    "            ],\n" +
                    "            \"x5t\": \"z8jMEvMDhEHbcNvHe0n92TfvUvs\",\n" +
                    "            \"x5t#S256\": \"d8_zDeYOBU_AujrwynJ48Y5g9s_EbkB0-u6jlP1x08I\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"kid\": \"7b-O5WQgXCwRryQiiKhtYqzfd5tTffdGI0_BfNu4GBI\",\n" +
                    "            \"kty\": \"RSA\",\n" +
                    "            \"alg\": \"RSA-OAEP\",\n" +
                    "            \"use\": \"enc\",\n" +
                    "            \"n\": \"vCIaboYUn7QckZQC8Nh-CAk7FR6Tft4iIph8vrASFA36_FPh1gWpYtQh9iuMSINtHoYzB566AX0RjrgwtZUsheuzj44vISq-kQC82jhRIj5JTWQhW9GdZXjOE7PivF-85xaBFv9elYD6okU7Xgx5AlGnlJNJtcJApODkWiQ2hXSC4H8yYXHgK2RefVfkyMu1P342vftJ_Cp3k4mjK37d0pWGaPykPOc-9xz-m94hxH1h3MS1sh4uh_h7QkBbeq7QRkuXETB6gZIlgMpePN-mkTJWFXgvsfB7YxnONkADpfY_ZMwoOXSiHn_cZsIYzFtcxPrJRf621bAm74_sHXnLbQ\",\n" +
                    "            \"e\": \"AQAB\",\n" +
                    "            \"x5c\": [\n" +
                    "                \"MIICsTCCAZkCBgGTPgKaAzANBgkqhkiG9w0BAQsFADAcMRowGAYDVQQDDBFoYXBpLWZoaXItdGVzdGluZzAeFw0yNDExMTgwNjQxMThaFw0zNDExMTgwNjQyNThaMBwxGjAYBgNVBAMMEWhhcGktZmhpci10ZXN0aW5nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvCIaboYUn7QckZQC8Nh+CAk7FR6Tft4iIph8vrASFA36/FPh1gWpYtQh9iuMSINtHoYzB566AX0RjrgwtZUsheuzj44vISq+kQC82jhRIj5JTWQhW9GdZXjOE7PivF+85xaBFv9elYD6okU7Xgx5AlGnlJNJtcJApODkWiQ2hXSC4H8yYXHgK2RefVfkyMu1P342vftJ/Cp3k4mjK37d0pWGaPykPOc+9xz+m94hxH1h3MS1sh4uh/h7QkBbeq7QRkuXETB6gZIlgMpePN+mkTJWFXgvsfB7YxnONkADpfY/ZMwoOXSiHn/cZsIYzFtcxPrJRf621bAm74/sHXnLbQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBvLUEm2wjh1NTfqeUKSa5tytNWYdE45w/G3EZ0cngHJU+Pe4y9xXn0rrA2ZNT4yZ3ppXhnAmY6tSynvQivyJp4q8ndPQb+tjFJcr9404ry9suk3qDG/KSROqfxGOA2lnRResrMlUrG5qM090e+HcHR3gT3gXidy46jQaoF0GuoCfV5yy/bEetXPxXFa8KsiojaVIrH82wl1v8CcmFLPaFl2NGqewf3hpG+48C/bSetzctNYE2UxHIMOOcABRwmULiwWEUWFM6ZqtibZ5Wt9s6MYSoBeWhM2WriDoGvrVcLPPwKJQG1YMpXY4gfNpLzmgWSyv/06H4Drr2bM0tTtXyp\"\n" +
                    "            ],\n" +
                    "            \"x5t\": \"RpC4prUCHYZOOtJvspfMWJ0YnUs\",\n" +
                    "            \"x5t#S256\": \"4wmwzu1Rn6WNQGYfeS2G8paBT8SKvmd67NI-55R18ys\"\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";

            // Parse the JWK set
            JWKSet jwkSet = JWKSet.parse(jwkSetJson);

            // Find the key with alg = RS256
            JWK rs256JWK = jwkSet.getKeys().stream()
                    .filter(jwk -> "RS256".equals(jwk.getAlgorithm().getName()))
                    .findFirst()
                    .orElse(null);

            if (rs256JWK == null) {
                System.err.println("No RS256 key found in the JWK set.");
                return;
            }

            if (!(rs256JWK instanceof RSAKey)) {
                System.err.println("The RS256 key is not an RSA key.");
                return;
            }

            RSAKey rsaKey = (RSAKey) rs256JWK;

            // Extract the RSAPublicKey
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            // Convert to PEM format
            String publicKeyPEM = convertToPEM(publicKey);

            // Path to save the PEM file
            String pemFilePath = "C:\\Users\\yasir.rehman\\spring-security-test\\rs256_public_key.pem"; // Update path as needed

            // Write to the PEM file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(pemFilePath))) {
                writer.write(publicKeyPEM);
            }

            System.out.println("RSA-256 Public Key has been written to: " + pemFilePath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

        /**
         * Converts RSAPublicKey to PEM formatted string
         *
         * @param publicKey RSAPublicKey instance
         * @return PEM formatted public key string
         */
        private static String convertToPEM(RSAPublicKey publicKey){
            StringBuilder pem = new StringBuilder();
            pem.append("-----BEGIN PUBLIC KEY-----\n");
            String encoded = Base64.getMimeEncoder(64, "\n".getBytes())
                    .encodeToString(publicKey.getEncoded());
            pem.append(encoded);
            pem.append("\n-----END PUBLIC KEY-----\n");
            return pem.toString();
        }
    }

