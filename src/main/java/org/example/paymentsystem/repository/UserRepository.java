package org.example.paymentsystem.repository;

import org.example.paymentsystem.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Page<UserEntity> findAllByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    Page<UserEntity> findAllByEmailContainingIgnoreCase(
            String email,
            Pageable pageable
    );

    long countByNameContainingIgnoreCase(String name);

    long deleteByEmailIgnoreCase(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserEntity u
        SET u.name = :name
        WHERE u.id = :id
    """)
    int updateNameById(
            @Param("id") Long id,
            @Param("name") String name
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserEntity u
        SET u.email = :email
        WHERE u.id = :id
    """)
    int updateEmailById(
            @Param("id") Long id,
            @Param("email") String email
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserEntity u
        SET u.password = :password
        WHERE u.id = :id
    """)
    int updatePasswordById(
            @Param("id") Long id,
            @Param("password") String password
    );
}
