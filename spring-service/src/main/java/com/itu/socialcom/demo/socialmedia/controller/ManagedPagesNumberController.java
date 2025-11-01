package com.itu.socialcom.demo.socialmedia.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.authentication.user.phonenumber.SellerPhoneNumberRepository;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPagesNumberRequest;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPagesNumber;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPagesNumberRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/managed-pages/numbers")
public class ManagedPagesNumberController {

    private final ManagedPagesNumberRepository managedPagesNumberRepository;
    private final ManagedPageRepository managedPageRepository;
    private final SellerPhoneNumberRepository sellerPhoneNumberRepository;
    private final TokenV2ServiceImpl tokenV2Service;

    @Autowired
    public ManagedPagesNumberController(ManagedPagesNumberRepository managedPagesNumberRepository,
                                        ManagedPageRepository managedPageRepository,
                                        SellerPhoneNumberRepository sellerPhoneNumberRepository,
                                        TokenV2ServiceImpl tokenV2Service) {
        this.managedPagesNumberRepository = managedPagesNumberRepository;
        this.managedPageRepository = managedPageRepository;
        this.sellerPhoneNumberRepository = sellerPhoneNumberRepository;
        this.tokenV2Service = tokenV2Service;
    }

    /**
     * Create a new mp_payment_number row linking a seller phone number (id_spn) to a managed page (id_mp)
     * Enforces uniqueness on (id_mp, id_pm).
     */
    @PostMapping
    public ResponseEntity<ManagedPagesNumber> create(@RequestBody @Valid ManagedPagesNumberRequest request, @RequestHeader("Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElseThrow(() -> new IllegalStateException("Invalid seller token"));

            // Verify managed page belongs to seller
            Optional<ManagedPage> managedPageOpt = managedPageRepository.findById(request.getIdMp());
            if (managedPageOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            ManagedPage managedPage = managedPageOpt.get();
            if (!managedPage.getSellerId().equals(seller.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verify seller phone number belongs to seller
            boolean spnExists = sellerPhoneNumberRepository.findById(request.getIdSpn())
                    .map(spn -> spn.getSeller().getId().equals(seller.getId()))
                    .orElse(false);
            if (!spnExists) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Ensure uniqueness: a managed page can't have two entries for the same payment method
            if (managedPagesNumberRepository.existsByIdMpAndIdPm(request.getIdMp(), request.getIdPm())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            ManagedPagesNumber mpNumber = new ManagedPagesNumber();
            mpNumber.setIdMp(request.getIdMp());
            mpNumber.setIdSpn(request.getIdSpn());
            mpNumber.setIdPm(request.getIdPm());

            ManagedPagesNumber saved = managedPagesNumberRepository.save(mpNumber);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update (or create) the seller phone number for a specific managed page & payment method.
     * PUT /api/managed-pages/numbers/{idMp}/{idPm}
     */
    @PutMapping("/{idMp}/{idPm}")
    public ResponseEntity<ManagedPagesNumber> upsertForPaymentMethod(@PathVariable Long idMp, @PathVariable Long idPm,
                                                                     @RequestBody @Valid ManagedPagesNumberRequest request,
                                                                     @RequestHeader("Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElseThrow(() -> new IllegalStateException("Invalid seller token"));

            // Verify managed page belongs to seller
            Optional<ManagedPage> managedPageOpt = managedPageRepository.findById(idMp);
            if (managedPageOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            ManagedPage managedPage = managedPageOpt.get();
            if (!managedPage.getSellerId().equals(seller.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verify seller phone number belongs to seller
            boolean spnExists = sellerPhoneNumberRepository.findById(request.getIdSpn())
                    .map(spn -> spn.getSeller().getId().equals(seller.getId()))
                    .orElse(false);
            if (!spnExists) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Find existing mapping for this managed page and payment method
            Optional<ManagedPagesNumber> existing = managedPagesNumberRepository.findByIdMpAndIdPm(idMp, idPm);
            ManagedPagesNumber mpNumber;
            if (existing.isPresent()) {
                mpNumber = existing.get();
                mpNumber.setIdSpn(request.getIdSpn());
            } else {
                mpNumber = new ManagedPagesNumber();
                mpNumber.setIdMp(idMp);
                mpNumber.setIdPm(idPm);
                mpNumber.setIdSpn(request.getIdSpn());
            }

            ManagedPagesNumber saved = managedPagesNumberRepository.save(mpNumber);
            return ResponseEntity.ok(saved);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
