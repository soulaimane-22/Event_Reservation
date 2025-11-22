package com.event.event_reservation.repository;

import com.event.event_reservation.entity.RechercheRecente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RechercheRecenteRepository extends JpaRepository<RechercheRecente, Long> {

    // Trouver les recherches récentes d'un utilisateur
    List<RechercheRecente> findByUtilisateurId(Long utilisateurId);

    // Trouver les recherches récentes triées par date décroissante
    @Query("SELECT r FROM RechercheRecente r WHERE r.utilisateur.id = :utilisateurId " +
            "ORDER BY r.dateRecherche DESC")
    List<RechercheRecente> findUserSearchesOrderByDateDesc(@Param("utilisateurId") Long utilisateurId);

    // Trouver les recherches les plus fréquentes d'un utilisateur
    @Query("SELECT r FROM RechercheRecente r WHERE r.utilisateur.id = :utilisateurId " +
            "ORDER BY r.nombreUtilisations DESC")
    List<RechercheRecente> findMostFrequentSearches(@Param("utilisateurId") Long utilisateurId);

    // Supprimer les anciennes recherches (garder seulement les 20 dernières)
    @Query("DELETE FROM RechercheRecente r WHERE r.utilisateur.id = :utilisateurId " +
            "AND r.id NOT IN (SELECT id FROM RechercheRecente WHERE utilisateur.id = :utilisateurId " +
            "ORDER BY dateRecherche DESC LIMIT 20)")
    void deleteOldSearches(@Param("utilisateurId") Long utilisateurId);
}