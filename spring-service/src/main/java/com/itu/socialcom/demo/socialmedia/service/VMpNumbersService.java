package com.itu.socialcom.demo.socialmedia.service;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPagesNumber;
import com.itu.socialcom.demo.socialmedia.entity.VMpNumbers;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPagesNumberRepository;
import com.itu.socialcom.demo.socialmedia.repository.VMpNumbersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VMpNumbersService {

    private final VMpNumbersRepository repository;
    @Autowired
    private ManagedPagesNumberRepository managedPagesNumber;
    public VMpNumbersService(VMpNumbersRepository repository) {
        this.repository = repository;
    }

    public List<VMpNumbers> findAll() {
        return repository.findAll();
    }

    public Optional<VMpNumbers> findById(Long id) {
        return repository.findById(id);
    }

    public ManagedPagesNumber save(ManagedPagesNumber vMpNumbers) {
        managedPagesNumber.save(vMpNumbers);
        return vMpNumbers;
    }

    public ManagedPagesNumber update(Long id, ManagedPagesNumber updated) {
        return managedPagesNumber.findById(id)
                .map(existing -> {
                    existing.setIdPm(updated.getIdPm());
                    existing.setIdMp(updated.getIdMp());
                    existing.setIdSpn(updated.getIdSpn());
                    return managedPagesNumber.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Record not found with id " + id));
    }

    public void delete(Long id) {
        managedPagesNumber.deleteById(id);
    }
}