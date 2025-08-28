package com.itu.socialcom.demo.moneytransactions;

import java.io.IOException;

public abstract class PaymentProvider {
    public abstract void initiateProvider(String propertiesFileName) throws IOException;

    public abstract PaymentResponse initiateTransaction(PaymentRequest request);

    public abstract PaymentResponse getTransactionDetails(String transactionId);

    public abstract PaymentResponse getTransactionStatus(String correlationId);
}
