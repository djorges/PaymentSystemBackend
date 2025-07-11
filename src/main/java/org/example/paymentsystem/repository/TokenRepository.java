package org.example.paymentsystem.repository;

import org.example.paymentsystem.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity,Long> {
    List<TokenEntity> findAllExpiredIsFalseOrRevokedIsFalseByUserId(Long userId);
}
