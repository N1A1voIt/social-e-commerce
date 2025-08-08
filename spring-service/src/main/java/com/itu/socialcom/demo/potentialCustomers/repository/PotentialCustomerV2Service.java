package com.itu.socialcom.demo.potentialCustomers.repository;

import com.itu.socialcom.demo.potentialCustomers.entity.PotentialCustomerV2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PotentialCustomerV2Service {

    private final PotentialCustomerV2Repository repository;

    public PotentialCustomerV2Service(PotentialCustomerV2Repository repository) {
        this.repository = repository;
    }

    public List<PotentialCustomerV2> findAll() {
        return repository.findAll();
    }

    public Optional<PotentialCustomerV2> findById(String id) {
        return repository.findById(id);
    }

    public PotentialCustomerV2 save(PotentialCustomerV2 customer) {
        return repository.save(customer);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}
