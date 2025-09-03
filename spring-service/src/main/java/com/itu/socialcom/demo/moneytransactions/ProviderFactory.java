package com.itu.socialcom.demo.moneytransactions;

import com.itu.socialcom.demo.moneytransactions.mvola.MVolaProvider;
import com.itu.socialcom.demo.moneytransactions.orange.OrangeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProviderFactory {
    @Autowired
    MVolaProvider mVolaProvider;
    @Autowired
    OrangeProvider orangeProvider;

    public PaymentProvider getProvider(String provider) {
        return switch (provider.toLowerCase()) {
            case "mvola" -> mVolaProvider;
            case "orange" -> orangeProvider;
            default -> throw new IllegalArgumentException("Unsupported payment provider: " + provider);
        };
    }
}
