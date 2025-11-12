package com.itu.socialcom.demo.orders.tempLink;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TempLinkService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final TempLinkRepository repository;

    public TempLinkService(TempLinkRepository repository) {
        this.repository = repository;
    }

    public TempLink createLink(String phoneNumber, Integer idOrderM, Integer idSeller,double downPayment) {
        TempLink link = new TempLink();
        String id = UUID.randomUUID().toString();

        link.setId(id);
        link.setPhoneNumber(phoneNumber);
        link.setIdOrderM(idOrderM);
        link.setIdSeller(idSeller);
        link.setExpiredAt(LocalDateTime.now().plusHours(1));
        link.setAmount(downPayment);
        // Secure temp link generation
        String tempLink = baseUrl + "/transactions?id_payment=" + id;
        link.setTempLink(tempLink);

        return repository.save(link);
    }

    public TempLink createFullAmountLink(String phoneNumber, Integer idOrderM, Integer idSeller,double downPayment) {
        TempLink link = new TempLink();
        String id = UUID.randomUUID().toString();

        link.setId(id);
        link.setPhoneNumber(phoneNumber);
        link.setIdOrderM(idOrderM);
        link.setIdSeller(idSeller);
        link.setExpiredAt(LocalDateTime.now().plusHours(1));
        link.setAmount(downPayment);
        // Secure temp link generation
        String tempLink = baseUrl + "/transactions-full?id_payment=" + id;
        link.setTempLink(tempLink);

        return repository.save(link);
    }

    public boolean isLinkValid(String tempLink) {
        Optional<TempLink> linkOpt = repository.findByTempLink(tempLink);
        if (linkOpt.isPresent()) {
            TempLink link = linkOpt.get();
            return link.getExpiredAt().isAfter(LocalDateTime.now());
        }
        return false;
    }
}
