package com.example.worldcup.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAllByOrderByTotalPointsDescUsernameAsc();

    /**
     * Number of users strictly ahead of the given point total. Used by the
     * dashboard to compute "your rank" in a single query (competition rank:
     * tied users share a rank).
     */
    long countByTotalPointsGreaterThan(long totalPoints);

    long countByRole(Role role);

    List<User> findAllByIdInOrderByTotalPointsDescUsernameAsc(java.util.Collection<Long> ids);
}
