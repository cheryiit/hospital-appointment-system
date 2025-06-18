package com.hospital.repository;

import com.hospital.entity.Role;
import com.hospital.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

// Repository sınıfı - veritabanı işlemleri için JPA kullanılır
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.specialization) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> findByRoleAndSearch(@Param("role") Role role, @Param("search") String search);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);
}