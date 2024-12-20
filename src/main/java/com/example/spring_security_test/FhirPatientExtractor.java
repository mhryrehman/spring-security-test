package com.example.spring_security_test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FhirPatientExtractor {
   static String FHIR_SERVER_URL = "http://localhost:8080/fhir/DEFAULT";

    public static void main(String[] args) throws Exception {
        // Replace with the actual FHIR response JSON

        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ0ZExIcTR3MG5ManVYZVc1S3QwUE10bkVmUlRaWFAyVnpzVEFsc3J3dHJNIn0.eyJleHAiOjE3MzQ1NTYyNjUsImlhdCI6MTczNDUyMDI2NSwianRpIjoiNjU3NDYyZGEtZGQwYS00ZjcxLTk5ZDAtNDFlNTBiZmVkZWRmIiwiaXNzIjoiaHR0cDovLzE3Mi4xNi4xMDIuMjM4OjgwODIvcmVhbG1zL3Rlc3RpbmciLCJhdWQiOiJodHRwczovL2ZoaXIuY3VyZW1kLmNvbTo4MDgwIiwic3ViIjoiOTY4NWNmMjctYjIzYS00ZDZiLTk1NjctM2ViNjVkZThhNjUxIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoidGVzdCIsInNpZCI6Ijc3MjYzNWNmLWYyMmEtNGQ5ZC04ZWY5LTE0N2YyOWZiMDBiMyIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy10ZXN0aW5nIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicGF0aWVudC9QYXRpZW50LnJlYWQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwicGF0aWVudCI6WyIxMjMiLCI0NTYiXSwicGF0aWVudF9pZCI6WyI1MiIsIjUzIl0sIm5hbWUiOiJ0ZXN0IHRlc3QiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ0ZXN0IiwiZ2l2ZW5fbmFtZSI6InRlc3QiLCJmYW1pbHlfbmFtZSI6InRlc3QiLCJlbWFpbCI6InRlc3RAZ21haWwuY29tIn0.d6dh93kIS4kXUYJqlF9mZrdK3itemvYJ-yKAAP5i4g0k4Kj2ougc0PXSCPrUjEjEFovcZYtqVLb8a9_3OWBcGXi08ecI9WujJNjZba1dnR69xpKBwFpLJ8turx9u6rPrXSHe0Zm5XYzN33KjxQDqQrFPNLKGYYdGy2AkjJ_CojfsdS4sOH73ucl4oAV__hH_4Y-KzYnM_dBQDz6d6XHFbGzxLya2Xg9ks3p_BHs8g87MjkKZVS4EDtkAJysuBH1ehlU7hwgUT7x5HS7jGeMkyIrHMHes7sW-ZOmLaTtG4ETx9GVppm2RqvQ2F7UY37BKUHLntHD7a3-xlzI2ipT1cA";
       String response = getPatientDetailsFromFhir("52", token);
       System.out.println("response received from fhir " + response);
        try {
            // Extract ID, name, and DOB
            PatientStruct patientDetails = extractPatientDetails(response);
            System.out.println("Patient ID: " + patientDetails.getId());
            System.out.println("Patient Name: " + patientDetails.getName());
            System.out.println("Patient DOB: " + patientDetails.getDateOfBirth());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPatientDetailsFromFhir(String patientId, String accessToken) throws Exception {
        // Construct the Patient resource URL
        String patientUrl = FHIR_SERVER_URL + "/Patient/" + patientId;

        // Create an HTTP client
        HttpClient httpClient = HttpClient.newHttpClient();
        System.out.println("going fetch data using url : " + patientUrl);

        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(patientUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/fhir+json") // Request FHIR JSON format
                .GET()
                .build();

        // Send the HTTP request and get the response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Check the response status
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch patient details. HTTP status: " + response.statusCode());
        }

        // Return the response body
        return response.body();
    }

    public static PatientStruct extractPatientDetails(String fhirResponse) throws Exception {
        // Parse JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(fhirResponse);

        // Extract ID
        String id = rootNode.path("id").asText();

        // Extract name
        JsonNode nameNode = rootNode.path("name");
        String fullName = "";
        if (nameNode.isArray() && nameNode.size() > 0) {
            JsonNode firstNameEntry = nameNode.get(0); // Use the first name entry
            String familyName = firstNameEntry.path("family").asText();
            String givenName = firstNameEntry.path("given").isArray()
                    ? firstNameEntry.path("given").get(0).asText()
                    : ""; // Extract the first given name
            fullName = givenName + " " + familyName;
        }

        // Extract DOB
        String dateOfBirth = rootNode.path("birthDate").asText();

        // Return the extracted details
        return new PatientStruct(id, fullName, dateOfBirth);
    }
}



// Helper class to store patient details
class PatientStruct {
    private String id;
    private String name;
    private String dateOfBirth;

    public PatientStruct(String id, String name, String dateOfBirth) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }
}
