package com.itu.socialcom.demo.moneytransactions.mvola;

import com.itu.socialcom.demo.authentication.token.TokenV2;
import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.moneytransactions.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MVolaProvider extends PaymentProvider {
    @Value("${mvola.baseUrl}")
    private String baseUrl;
    @Value("${mvola.partnerName}")
    private String partnerName;
    @Value("${mvola.partnerMsisdn}")
    private String partnerMsisdn;
    @Value("${mvola.consumer.key}")
    private String consumerKey;
    @Value("${mvola.consumer.secret}")
    private String consumerSecret;
    private final TokenV2Service tokenV2Service;
    @Autowired
    MvolaTokenRepository mvolaTokenRepository;
    /**
     * Retrieves a valid MVola token from the database
     * @return The token string or null if no valid token is found
     */
    private String retrieveTokenFromDb() throws IOException, InterruptedException {
        MvolaTokens ret = null;
        Optional<MvolaTokens> token = mvolaTokenRepository.findValid();
        if (token.isEmpty()) {
            String auth = consumerKey + ":" + consumerSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            String body = "grant_type=client_credentials&scope=EXT_INT_MVOLA_SCOPE";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl+"/token"))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cache-Control", "no-cache")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                String accessToken = jsonResponse.getString("access_token");
                int expiresIn = jsonResponse.getInt("expires_in");
                MvolaTokens mvolaTokens = new MvolaTokens();
                System.out.println(accessToken);
                mvolaTokens.setToken(accessToken);
                mvolaTokens.setStartDate(LocalDateTime.now());
                mvolaTokens.setExpirationDate(LocalDateTime.now().plusSeconds(expiresIn));
                mvolaTokenRepository.save(mvolaTokens);
                ret = mvolaTokens;
            } else {
                System.out.println("wxdcfvgbhjn,k;l:mù!");
                throw new IOException("Failed to retrieve token, status code: " + response.statusCode());
            }
        } else {
            ret = token.get();
        }
        return ret.getToken();
    }

    @Override
    public void initiateProvider(String properties) throws IOException {
//        Properties props = ConfigLoader.load(properties);
//        this.baseUrl = props.getProperty("mvola.baseUrl", "https://devapi.mvola.mg");
//        this.partnerName = props.getProperty("mvola.partnerName");
//        this.partnerMsisdn = props.getProperty("mvola.partnerMsisdn");
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
