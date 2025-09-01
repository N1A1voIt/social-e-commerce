package com.itu.socialcom.demo.moneytransactions.mvola;

import com.itu.socialcom.demo.moneytransactions.ConfigLoader;
import com.itu.socialcom.demo.moneytransactions.PaymentProvider;
import com.itu.socialcom.demo.moneytransactions.PaymentRequest;
import com.itu.socialcom.demo.moneytransactions.PaymentResponse;

import java.io.IOException;
import java.util.Properties;

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
    public PaymentResponse initiateTransaction(PaymentRequest request) {
        
        return null;
    }

    @Override
    public PaymentResponse getTransactionDetails(String transactionId) {
        return null;
    }

    @Override
    public PaymentResponse getTransactionStatus(String correlationId) {
        return null;
    }
}
