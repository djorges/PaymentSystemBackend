package org.example.paymentsystem.repository;

import org.example.paymentsystem.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, Long> {
    
    Optional<TokenEntity> findByToken(String token);

    List<TokenEntity> findAllByUserIdAndExpiredFalseAndRevokedFalse(Long userId);

    List<TokenEntity> findAllByUserIdAndTokenTypeAndExpiredFalseAndRevokedFalse(
            Long userId,
            TokenType tokenType
    );

    boolean existsByToken(String token);

    boolean existsByUserIdAndExpiredFalseAndRevokedFalse(Long userId);

    @Modifying
    @Query("""
        UPDATE TokenEntity t
        SET t.revoked = true
        WHERE t.token = :token
    """)
    int revokeByToken(@Param("token") String token);

    @Modifying
    @Query("""
        UPDATE TokenEntity t
        SET t.revoked = true
        WHERE t.user.id = :userId
          AND t.revoked = false
    """)
    int revokeAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("""
        UPDATE TokenEntity t
        SET t.expired = true
        WHERE t.token = :token
    """)
    int expireByToken(@Param("token") String token);

    @Modifying
    @Query("""
        DELETE FROM TokenEntity t
        WHERE t.expired = true OR t.revoked = true
    """)
    int deleteInvalidTokens();

    @Modifying
    @Query("""
        DELETE FROM TokenEntity t
        WHERE t.user.id = :userId
    """)
    int deleteAllByUserId(@Param("userId") Long userId);
}
