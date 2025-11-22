package com.event.event_reservation.repository;

import com.event.event_reservation.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {

    // Trouver les avis d'un événement
    List<Avis> findByEvenementId(Long evenementId);

    // Trouver l'avis unique d'un utilisateur pour un événement
    Optional<Avis> findByUtilisateurIdAndEvenementId(Long utilisateurId, Long evenementId);

    // Trouver tous les avis d'un utilisateur
    List<Avis> findByUtilisateurId(Long utilisateurId);

    // Calculer la moyenne des notes pour un événement
    @Query("SELECT COALESCE(AVG(a.note), 0) FROM Avis a WHERE a.evenement.id = :evenementId")
    BigDecimal getAverageRatingForEvent(@Param("evenementId") Long evenementId);

    // Compter le nombre d'avis pour un événement
    long countByEvenementId(Long evenementId);

    // Trouver les avis par note
    List<Avis> findByNote(Integer note);

    // Bonus : Trouver les avis d'un événement triés par date décroissante
    @Query("SELECT a FROM Avis a WHERE a.evenement.id = :evenementId ORDER BY a.datePublication DESC")
    List<Avis> findEventAvisOrderByDate(@Param("evenementId") Long evenementId);

    // Bonus : Trouver les avis d'un événement avec note >= 4
    @Query("SELECT a FROM Avis a WHERE a.evenement.id = :evenementId AND a.note >= 4")
    List<Avis> findPositiveAvisForEvent(@Param("evenementId") Long evenementId);
}