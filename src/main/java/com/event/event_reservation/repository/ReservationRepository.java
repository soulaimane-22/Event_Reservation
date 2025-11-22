package com.event.event_reservation.repository;

import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 1. Trouver les réservations d'un utilisateur
    List<Reservation> findByUtilisateurId(Long utilisateurId);

    // 2. Trouver les réservations d'un événement avec un statut donné
    List<Reservation> findByEvenementIdAndStatut(Long evenementId, ReservationStatus statut);

    // 3. Calculer le nombre total de places réservées pour un événement
    @Query("SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r " +
            "WHERE r.evenement.id = :evenementId AND r.statut = 'CONFIRMEE'")
    Integer getTotalPlacesReservedForEvent(@Param("evenementId") Long evenementId);

    // 4. Trouver les réservations par code
    Optional<Reservation> findByCodeReservation(String codeReservation);

    // 5. Trouver les réservations entre deux dates
    @Query("SELECT r FROM Reservation r WHERE r.dateReservation BETWEEN :dateDebut AND :dateFin")
    List<Reservation> findReservationsBetweenDates(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    // 6. Trouver les réservations confirmées d'un utilisateur
    List<Reservation> findByUtilisateurIdAndStatut(Long utilisateurId, ReservationStatus statut);

    // 7. Calculer le montant total des réservations par utilisateur
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0) FROM Reservation r " +
            "WHERE r.utilisateur.id = :utilisateurId AND r.statut = 'CONFIRMEE'")
    BigDecimal getTotalReservationAmountByUser(@Param("utilisateurId") Long utilisateurId);

    // Bonus : Trouver les réservations par statut
    List<Reservation> findByStatut(ReservationStatus statut);

    // Bonus : Compter les réservations d'un événement
    long countByEvenementId(Long evenementId);

    // Bonus : Compter les réservations confirmées d'un événement
    long countByEvenementIdAndStatut(Long evenementId, ReservationStatus statut);

    // Bonus : Trouver les réservations d'un utilisateur avec un statut donné et triées par date décroissante
    @Query("SELECT r FROM Reservation r WHERE r.utilisateur.id = :utilisateurId " +
            "AND r.statut = :statut ORDER BY r.dateReservation DESC")
    List<Reservation> findUserReservationsByStatusOrderByDate(
            @Param("utilisateurId") Long utilisateurId,
            @Param("statut") ReservationStatus statut
    );

    // Bonus : Trouver les réservations d'un événement triées par date
    @Query("SELECT r FROM Reservation r WHERE r.evenement.id = :evenementId " +
            "ORDER BY r.dateReservation DESC")
    List<Reservation> findEventReservationsOrderByDate(@Param("evenementId") Long evenementId);

    // Bonus : Vérifier si un utilisateur a déjà réservé un événement
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.utilisateur.id = :utilisateurId " +
            "AND r.evenement.id = :evenementId AND r.statut = 'CONFIRMEE'")
    boolean userHasConfirmedReservationForEvent(
            @Param("utilisateurId") Long utilisateurId,
            @Param("evenementId") Long evenementId
    );

    // Bonus : Calculer le montant moyen des réservations d'un utilisateur
    @Query("SELECT COALESCE(AVG(r.montantTotal), 0) FROM Reservation r " +
            "WHERE r.utilisateur.id = :utilisateurId AND r.statut = 'CONFIRMEE'")
    BigDecimal getAverageReservationAmountByUser(@Param("utilisateurId") Long utilisateurId);
}
