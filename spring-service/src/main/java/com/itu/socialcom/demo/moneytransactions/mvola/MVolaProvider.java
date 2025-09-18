package com.itu.socialcom.demo.moneytransactions.mvola;

import com.itu.socialcom.demo.authentication.token.TokenV2;
import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.moneytransactions.ConfigLoader;
import com.itu.socialcom.demo.moneytransactions.PaymentProvider;
import com.itu.socialcom.demo.moneytransactions.PaymentRequest;
import com.itu.socialcom.demo.moneytransactions.PaymentResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.UUID;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MVolaProvider extends PaymentProvider {
    private String baseUrl;
    private String partnerName;
    private String partnerMsisdn;

    private final TokenV2Service tokenV2Service;

    /**
     * Retrieves a valid MVola token from the database
     * @return The token string or null if no valid token is found
     */
    private String retrieveTokenFromDb() {
        // Get the most recent valid token from the database
//        Optional<TokenV2> tokenOpt = tokenV2Service.getToken("mvola_token");
//        if (tokenOpt.isPresent() && tokenOpt.get().getExpiryDate().isAfter(LocalDateTime.now())) {
//            return tokenOpt.get().getToken();
//        }
        return "eyJ4NXQiOiJaREUzWW1RNFkyRmtZekprTmpNMk5EVmtZVE5oTkRSak16azFObVEyWXprelkyUTFaVFZqWVEiLCJraWQiOiJNVGRsTXpneFpqZGtNakk0WmpKbVlUZ3dNRFJpWWpNMU1tUmhOamxoTUdNME1XTmtPV05tT1RobU16VXlNMlUxTkRZNE5UWXhOMk01TW1SbU5XUTRPQV9SUzI1NiIsInR5cCI6ImF0K2p3dCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJueWF2b3JhbmRyaWFuYXJpc29hQGdtYWlsLmNvbSIsImF1dCI6IkFQUExJQ0FUSU9OIiwiYXVkIjoiZlM5Mkt3dGpJMzZkT0ttVWVINEo4YkxHUXZrYSIsIm5iZiI6MTc1ODIyMDA4NywiYXpwIjoiZlM5Mkt3dGpJMzZkT0ttVWVINEo4YkxHUXZrYSIsInNjb3BlIjoiRVhUX0lOVF9NVk9MQV9TQ09QRSIsImlzcyI6Imh0dHBzOlwvXC9kZXZlbG9wZXIubXZvbGEubWdcL29hdXRoMlwvdG9rZW4iLCJyZWFsbSI6eyJzaWduaW5nX3RlbmFudCI6ImNhcmJvbi5zdXBlciJ9LCJleHAiOjE3NTgyMjM2ODcsImlhdCI6MTc1ODIyMDA4NywianRpIjoiY2IxYmRhMjgtYTA1YS00ZWU5LTlkMzYtZDE5ZjdmMjg1MWM2In0.Um10PvyW1AlYnwtOr5C6xTuPM4JlsTKPyMHtRNUugHbjXDOxYhw2ZuMeIzQDvSKMRCs_hcKQy5q2ZBxwroPEIRyJEHXqKBKi1z5yRsmHoZ0qFGsKFc92Mm-kt9TvbVyPhRnw3IC4ql8xR2GsvjnoSg9nOhUJ8wy7W-fQD686YMUz7RzuQWdL6T19lMR9Yx011_3VC4PzVkBgoTF4lls5u2TAS8X3Q1PY5JrgC3bwqeMx7T_DTJcYZoZLXMklJfczgug4dSVjpPJkVfMcplS_MIpBgvadbB36B1lYOEsrRbl_tcGrwnJJZS_AuHwTIPJ_AQI50sgQkL2A05Fj2U5VhQ";
    }

    @Override
    public void initiateProvider(String properties) throws IOException {
        Properties props = ConfigLoader.load(properties);
        this.baseUrl = props.getProperty("mvola.baseUrl", "https://devapi.mvola.mg");
        this.partnerName = props.getProperty("mvola.partnerName");
        this.partnerMsisdn = props.getProperty("mvola.partnerMsisdn");
    }

    @Override
    public PaymentResponse initiateTransaction(PaymentRequest request) throws Exception  {
        request.setRequestDate(LocalDateTime.now());
        System.out.println(request);
        try {
            String url = baseUrl + "/mvola/mm/transactions/type/merchantpay/1.0.0/";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            String token = retrieveTokenFromDb();
            System.out.println(token);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Version", "1.0");
            conn.setRequestProperty("X-CorrelationID", UUID.randomUUID().toString());
            conn.setRequestProperty("UserLanguage", "mg");
            conn.setRequestProperty("UserAccountIdentifier", "msisdn;" + request.getCustomerMsisdn());
            conn.setRequestProperty("partnerName", "test");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Cache-Control", "no-cache");

            // Format the date as ISO-8601
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String formattedDate = request.getRequestDate().format(formatter);

            // Create the transaction reference if not provided
            String transactionReference = request.getRequestingOrganisationTransactionReference();
//            if (transactionReference == null || transactionReference.isEmpty()) {
                transactionReference = "tx_" + UUID.randomUUID().toString().substring(0, 10);
//            }

            JSONObject body = new JSONObject();
            body.put("amount", request.getAmount());
            body.put("currency", "Ar");
            body.put("descriptionText", request.getDescription());
            body.put("requestDate", formattedDate);
            body.put("requestingOrganisationTransactionReference", transactionReference);
            body.put("originalTransactionReference", transactionReference);

            // Create debitParty array with customer's phone number
            JSONArray debitParty = new JSONArray();
            JSONObject debit = new JSONObject();
            debit.put("value", request.getCustomerMsisdn());
            debit.put("key", "msisdn");
            debitParty.put(debit);
            body.put("debitParty", debitParty);

            // Create creditParty array with partner's phone number
            JSONArray creditParty = new JSONArray();
            JSONObject credit = new JSONObject();
            credit.put("value", request.getPayee() != null ? request.getPayee() : partnerMsisdn);
            credit.put("key", "msisdn");
            creditParty.put(credit);
            body.put("creditParty", creditParty);

            // Create metadata array
            JSONArray metadata = new JSONArray();

            // Add partnerName to metadata
            JSONObject metaPartner = new JSONObject();
            metaPartner.put("value", "partnerName");
            metaPartner.put("key", "partnerName");
            metadata.put(metaPartner);

            // Add fc (foreign currency) to metadata
            JSONObject metaFc = new JSONObject();
            metaFc.put("value", "USD");
            metaFc.put("key", "fc");
            metadata.put(metaFc);

            // Add amountFc to metadata
            JSONObject metaAmountFc = new JSONObject();
            metaAmountFc.put("value", "1");
            metaAmountFc.put("key", "amountFc");
            metadata.put(metaAmountFc);

            body.put("metadata", metadata);
            System.out.println(body);
            try  {
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                System.out.println("MVola request payload: " + body.toString());
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

            PaymentResponse paymentResponse = getPaymentResponse(conn);
            System.out.println("Right here,right now");
            return paymentResponse;
        } catch (Exception e) {
            throw e;
        }
    }

    @NotNull
    private static PaymentResponse getPaymentResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        String responseJson = sb.toString();
        System.out.println(responseJson);
        JSONObject jsonResponse = new JSONObject(responseJson);

        PaymentResponse paymentResponse = new PaymentResponse();

        if (jsonResponse.has("transactionReference")) {
            paymentResponse.setTransactionId(jsonResponse.getString("transactionReference"));
        }
        if (jsonResponse.has("transactionStatus")) {
            paymentResponse.setStatus(jsonResponse.getString("transactionStatus"));
        }
        if (jsonResponse.has("serverCorrelationId")) {
            paymentResponse.setCorrelationId(jsonResponse.getString("serverCorrelationId"));
        }
        return paymentResponse;
    }

    @Override
    public PaymentResponse getTransactionDetails(String transactionId) {
        return performGet("/mvola/mm/transactions/type/merchantpay/1.0.0/" + transactionId);
    }

    @Override
    public PaymentResponse getTransactionStatus(String correlationId) {
        return performGet("/mvola/mm/transactions/type/merchantpay/1.0.0/status/" + correlationId);
    }

    private PaymentResponse performGet(String path) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            conn.setRequestMethod("GET");

            // Headers
            String token = retrieveTokenFromDb();
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Version", "1.0");
            conn.setRequestProperty("X-CorrelationID", UUID.randomUUID().toString());
            conn.setRequestProperty("UserLanguage", "FR");
            conn.setRequestProperty("UserAccountIdentifier", "msisdn;" + partnerMsisdn);
            conn.setRequestProperty("partnerName", partnerName);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Cache-Control", "no-cache");

            return getPaymentResponse(conn);
        } catch (Exception e) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setStatus("Erreur !!");

            return paymentResponse;
        }
    }
}
