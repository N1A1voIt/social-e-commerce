package com.itu.socialcom.demo.authentication.token;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.authentication.user.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenV2ServiceImpl implements TokenV2Service {

    private final TokenV2Repository tokenV2Repository;
    private final SellerRepository sellerRepository;

    @Override
    @Transactional
    public TokenV2 saveToken(TokenV2 token) {
        return tokenV2Repository.save(token);
    }

    @Override
    public Optional<TokenV2> getToken(String token) {
        return tokenV2Repository.findByToken(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        return tokenV2Repository.findValidToken(token)
                .map(t -> !t.isExpired() && !t.isRevoked() && t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        tokenV2Repository.findByToken(token)
                .ifPresent(t -> {
                    t.setRevoked(true);
                    tokenV2Repository.save(t);
                });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        tokenV2Repository.revokeAllUserTokens(userId);
    }

    @Override
    @Transactional
    public TokenV2 createToken(Long userId, String token, long expirationInMinutes) {
        revokeAllUserTokens(userId);

        TokenV2 tokenV2 = new TokenV2();
        tokenV2.setToken(token);
        tokenV2.setUserId(userId);
        tokenV2.setExpiryDate(LocalDateTime.now().plusMinutes(expirationInMinutes));
        tokenV2.setExpired(false);
        tokenV2.setRevoked(false);
        
        return saveToken(tokenV2);
    }
    
    @Override
    public Optional<Long> findUserIdByToken(String token) {
        return tokenV2Repository.findUserIdByToken(token);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Seller> findSellerByToken(String token) {
        return findUserIdByToken(token)
                .flatMap(sellerRepository::findById);
    }
}
