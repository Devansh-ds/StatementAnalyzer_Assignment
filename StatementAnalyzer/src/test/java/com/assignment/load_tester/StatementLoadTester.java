package com.assignment.load_tester;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.UUID;

public class StatementLoadTester {

    private static final String BASE_URL = "http://localhost:8080/api/v1/analyze";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {

        System.out.println("=== STARTING STATEMENT LOAD TESTER ===");

        // Run multiple test cases
        for (int i = 1; i <= 3; i++) {
            System.out.println("\n--- Submitting Job #" + i + " ---");

            String payload = buildPayload(i);
            String jobId = submitStatement(payload);

            if (jobId != null) {
                pollForResult(jobId);
            }
        }

        System.out.println("\n=== LOAD TEST COMPLETED ===");
    }

    // ---------------- POST ----------------
    private static String submitStatement(String jsonPayload) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response =
                CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("POST Status: " + response.statusCode());
        System.out.println("POST Body  : " + response.body());

        if (response.statusCode() != 202) {
            System.err.println("POST failed!");
            return null;
        }

        return extractJobId(response.body());
    }

    // ---------------- GET (Polling) ----------------
    private static void pollForResult(String jobId) throws Exception {

        String url = BASE_URL + "/" + jobId;

        while (true) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("GET Response: " + response.body());

            if (response.body().contains("COMPLETED")) {
                System.out.println("Job " + jobId + " COMPLETED");
                break;
            }

            if (response.body().contains("IN_PROGRESS")) {
                System.out.println("Job still processing...");
            }

            Thread.sleep(1000); // wait before polling again
        }
    }

    // ---------------- Payload Generator ----------------
    private static String buildPayload(int index) {

        String txnId1 = "TXN_" + UUID.randomUUID();
        String txnId2 = "TXN_" + UUID.randomUUID();

        return """
        {
          "customerName": "Load Test User %d",
          "statementDate": "%s",
          "transactions": [
            {
              "transactionId": "%s",
              "date": "2024-01-01",
              "amount": 50000,
              "type": "CREDIT",
              "description": "Salary"
            },
            {
              "transactionId": "%s",
              "date": "2024-01-05",
              "amount": %d,
              "type": "DEBIT",
              "description": "Random Expense"
            },
            {
              "transactionId": "DUPLICATE_TXN",
              "date": "2024-01-10",
              "amount": 1000,
              "type": "DEBIT"
            },
            {
              "transactionId": "DUPLICATE_TXN",
              "date": "2024-01-11",
              "amount": 2000,
              "type": "DEBIT"
            },
            {
              "transactionId": "FUTURE_TXN",
              "date": "2030-01-01",
              "amount": 99999,
              "type": "CREDIT"
            },
            {
              "transactionId": "NULL_AMOUNT_TXN",
              "date": "2024-01-12",
              "amount": null,
              "type": "DEBIT"
            }
          ]
        }
        """.formatted(
                index,
                LocalDate.now(),
                txnId1,
                txnId2,
                5000 * index
        );
    }

    // ---------------- Helper ----------------
    private static String extractJobId(String responseBody) {
        int start = responseBody.indexOf(":") + 2;
        int end = responseBody.lastIndexOf("\"");

        if (start < 0 || end < 0) {
            System.err.println("Could not extract jobId!");
            return null;
        }

        String jobId = responseBody.substring(start, end);
        System.out.println("Extracted jobId: " + jobId);
        return jobId;
    }
}
