package com.event.event_reservation.repository;

import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // 1. Trouver les événements par catégorie
    List<Event> findByCategorie(EventCategory categorie);

    // 2. Trouver les événements publiés entre deux dates
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' " +
            "AND e.dateDebut BETWEEN :dateDebut AND :dateFin")
    List<Event> findPublishedEventsBetweenDates(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    // 3. Trouver les événements d'un organisateur avec un statut donné
    List<Event> findByOrganisateurIdAndStatut(Long organisateurId, EventStatus statut);

    // 4. Trouver les événements disponibles (publiés et non terminés)
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' AND e.statut != 'TERMINE' " +
            "AND e.capaciteRestante > 0")
    List<Event> findAvailableEvents();

    // 5. Compter le nombre d'événements par catégorie
    long countByCategorie(EventCategory categorie);

    // 6. Trouver les événements par lieu ou ville
    List<Event> findByLieu(String lieu);
    List<Event> findByVille(String ville);

    // 6bis. Trouver les événements par lieu ou ville (avec OR)
    @Query("SELECT e FROM Event e WHERE LOWER(e.lieu) LIKE LOWER(CONCAT('%', :lieu, '%')) " +
            "OR LOWER(e.ville) LIKE LOWER(CONCAT('%', :ville, '%'))")
    List<Event> findByLieuOrVille(@Param("lieu") String lieu, @Param("ville") String ville);

    // 7. Rechercher les événements par titre (contenant un mot-clé)
    @Query("SELECT e FROM Event e WHERE LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByTitre(@Param("keyword") String keyword);

    // 8. Trouver les événements par plage de prix
    @Query("SELECT e FROM Event e WHERE e.prixUnitaire BETWEEN :minPrice AND :maxPrice")
    List<Event> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice);

    // Bonus : Trouver les événements par statut
    List<Event> findByStatut(EventStatus statut);

    // Bonus : Trouver les événements publiés par catégorie
    List<Event> findByStatutAndCategorie(EventStatus statut, EventCategory categorie);

    // Bonus : Trouver les événements d'une ville avec un statut donné
    List<Event> findByVilleAndStatut(String ville, EventStatus statut);

    // Bonus : Trouver tous les événements de l'organisateur
    List<Event> findByOrganisateurId(Long organisateurId);

    // Bonus : Compter les événements d'un organisateur
    long countByOrganisateurId(Long organisateurId);

    // Bonus : Trouver les événements par plage de dates
    @Query("SELECT e FROM Event e WHERE e.dateDebut >= :dateDebut AND e.dateFin <= :dateFin")
    List<Event> findEventsBetweenDates(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );
}
