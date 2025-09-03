package com.itu.socialcom.demo.moneytransactions.mvola;

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
import java.util.Properties;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class MVolaProvider extends PaymentProvider {
    private String baseUrl;
    private String accessToken;
    private String partnerName;
    private String partnerMsisdn;

    @Override
    public void initiateProvider(String properties) throws IOException {
        Properties props = ConfigLoader.load(properties);
        this.baseUrl = props.getProperty("mvola.baseUrl", "https://devapi.mvola.mg");
        this.accessToken = props.getProperty("mvola.accessToken");
        this.partnerName = props.getProperty("mvola.partnerName");
        this.partnerMsisdn = props.getProperty("mvola.partnerMsisdn");
    }

    @Override
    public PaymentResponse initiateTransaction(PaymentRequest request) throws Exception  {
        request.setRequestDate(LocalDateTime.now());
        try {
            String url = baseUrl + "/mvola/mm/transactions/type/merchantpay/1.0.0/";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            System.out.println(accessToken);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Version", "1.0");
            conn.setRequestProperty("X-CorrelationID", UUID.randomUUID().toString());
            conn.setRequestProperty("UserLanguage", "FR");
            conn.setRequestProperty("UserAccountIdentifier", "msisdn;" + partnerMsisdn);
            conn.setRequestProperty("partnerName", partnerName);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Cache-Control", "no-cache");

            JSONObject body = new JSONObject();
            body.put("amount", request.getAmount());
            body.put("currency", "Ar");
            body.put("descriptionText", request.getDescription());
            body.put("requestDate", "");
            body.put("requestingOrganisationTransactionReference", "");

            JSONObject debit = new JSONObject();
            debit.put("key", "msisdn");
            debit.put("value", request.getCustomerMsisdn());
            body.append("debitParty", debit);

            JSONObject credit = new JSONObject();
            credit.put("key", "msisdn");
            credit.put("value", partnerMsisdn);
            body.append("creditParty", credit);

            JSONObject metaPartner = new JSONObject();
            metaPartner.put("key", "partnerName");
            metaPartner.put("value", partnerName);
            body.append("metadata", metaPartner);

            try  {
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
//                System.out.println(body.toString());
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
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
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
