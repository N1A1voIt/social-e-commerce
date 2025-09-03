package com.itu.socialcom.demo.moneytransactions.orange;

import com.itu.socialcom.demo.moneytransactions.PaymentProvider;
import com.itu.socialcom.demo.moneytransactions.PaymentRequest;
import com.itu.socialcom.demo.moneytransactions.PaymentResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OrangeProvider extends PaymentProvider {
    @Override
    public void initiateProvider(String propertiesFileName) throws IOException {

    }

    @Override
    public PaymentResponse initiateTransaction(PaymentRequest request) throws Exception {
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
