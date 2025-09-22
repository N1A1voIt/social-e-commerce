package com.itu.socialcom.demo.moneytransactions;

import lombok.Data;

@Data
public class TransactionDetail {
    double amount;
    String description;
    String provider;
    String idPayment;
}
