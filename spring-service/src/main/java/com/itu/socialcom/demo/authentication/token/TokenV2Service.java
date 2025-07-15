package com.itu.socialcom.demo.authentication.token;

import com.itu.socialcom.demo.authentication.user.Seller;
import java.util.Optional;

public interface TokenV2Service {
    TokenV2 saveToken(TokenV2 token);
    Optional<TokenV2> getToken(String token);
    boolean isTokenValid(String token);
    TokenV2 createToken(Long userId, String token, long expirationInMinutes);
    Optional<Long> findUserIdByToken(String token);
    Optional<Seller> findSellerByToken(String token);
}
