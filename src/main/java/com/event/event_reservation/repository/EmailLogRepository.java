package com.event.event_reservation.repository;

import com.event.event_reservation.entity.EmailLog;
import com.event.event_reservation.entity.enums.EmailStatus;
import com.event.event_reservation.entity.enums.EmailType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    // Trouver les emails par statut
    List<EmailLog> findByStatut(EmailStatus statut);

    // Trouver les emails d'une réservation
    List<EmailLog> findByReservationId(Long reservationId);

    // Trouver les emails non envoyés
    List<EmailLog> findByStatutNot(EmailStatus statut);

    // Compter les emails par statut
    long countByStatut(EmailStatus statut);

    // Trouver les emails en erreur pour une réservation
    @Query("SELECT e FROM EmailLog e WHERE e.reservation.id = :reservationId AND e.statut = 'ERREUR'")
    List<EmailLog> findErrorEmailsForReservation(@Param("reservationId") Long reservationId);

    // Trouver tous les emails en erreur
    @Query("SELECT e FROM EmailLog e WHERE e.statut = 'ERREUR' ORDER BY e.dateCreation DESC")
    List<EmailLog> findAllErrorEmails();

    // Compter les emails d'un type donné
    long countByTypeEmail(EmailType typeEmail);

    // Bonus : Trouver les emails d'un type et statut donné
    List<EmailLog> findByTypeEmailAndStatut(EmailType typeEmail, EmailStatus statut);
}