package com.event.event_reservation.repository;

import com.event.event_reservation.entity.Notification;
import com.event.event_reservation.entity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Trouver les notifications non lues d'un utilisateur
    List<Notification> findByDestinataireIdAndLueIsFalse(Long destinataireId);

    // Trouver toutes les notifications d'un utilisateur
    List<Notification> findByDestinataireId(Long destinataireId);

    // Compter les notifications non lues
    long countByDestinataireIdAndLueIsFalse(Long destinataireId);

    // Trouver les notifications non lues triées par date décroissante
    @Query("SELECT n FROM Notification n WHERE n.destinataire.id = :destinataireId " +
            "AND n.lue = false ORDER BY n.dateCreation DESC")
    List<Notification> findUnreadNotificationsByUserOrderByDate(@Param("destinataireId") Long destinataireId);

    // Trouver les notifications d'un type donné
    List<Notification> findByType(NotificationType type);

    // Trouver les notifications d'un utilisateur par type
    List<Notification> findByDestinataireIdAndType(Long destinataireId, NotificationType type);

    // Bonus : Compter les notifications par type pour un utilisateur
    long countByDestinataireIdAndType(Long destinataireId, NotificationType type);
}