package com.event.event_reservation.repository;

import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Trouver un utilisateur par email
    Optional<User> findByEmail(String email);

    // 2. Vérifier l'existence d'un email
    boolean existsByEmail(String email);

    // 3. Trouver tous les utilisateurs actifs par rôle
    List<User> findByRoleAndActifTrue(UserRole role);

    // 4. Trouver les utilisateurs par nom ou prénom (recherche insensible à la casse)
    @Query("SELECT u FROM User u WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchByNomOrPrenom(@Param("search") String search);

    // 5. Compter les utilisateurs par rôle
    long countByRole(UserRole role);

    // Bonus : Trouver tous les utilisateurs actifs
    List<User> findByActifTrue();

    // Bonus : Trouver un utilisateur par email insensible à la casse
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);
}