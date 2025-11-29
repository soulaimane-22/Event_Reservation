package com.event.event_reservation.service;

import com.event.event_reservation.dto.ReservationStatisticsDTO;
import com.event.event_reservation.dto.ReservationSummaryDTO;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.ReservationStatus;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.repository.ReservationRepository;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private static final int MAX_PLACES_PER_RESERVATION = 10;
    private static final int CANCELLATION_HOURS_BEFORE = 48;

    // 1. Création d'une réservation
    public Reservation createReservation(Long userId, Long eventId, Integer nombrePlaces) {

        // Validation du nombre de places
        if (nombrePlaces == null || nombrePlaces <= 0 || nombrePlaces > MAX_PLACES_PER_RESERVATION) {
            throw new IllegalArgumentException(
                    "Le nombre de places doit être entre 1 et " + MAX_PLACES_PER_RESERVATION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        // Vérifier que l'événement est valide
        if (event.getStatut() != EventStatus.PUBLIE) {
            throw new IllegalArgumentException("L'événement doit être publié");
        }

        if (event.getStatut() == EventStatus.TERMINE) {
            throw new IllegalArgumentException("L'événement est terminé, impossible de réserver");
        }

        // Vérifier la disponibilité des places
        if (event.getCapaciteRestante() < nombrePlaces) {
            throw new IllegalArgumentException(
                    "Pas assez de places disponibles. Places restantes: " + event.getCapaciteRestante());
        }

        // Générer le code de réservation
        String codeReservation = generateReservationCode();

        // Calculer le montant total
        BigDecimal montantTotal = event.getPrixUnitaire()
                .multiply(new BigDecimal(nombrePlaces));

        // Créer la réservation
        Reservation reservation = Reservation.builder()
                .codeReservation(codeReservation)
                .nombrePlaces(nombrePlaces)
                .montantTotal(montantTotal)
                .utilisateur(user)
                .evenement(event)
                .statut(ReservationStatus.EN_ATTENTE)
                .build();

        reservation = reservationRepository.save(reservation);

        // Mettre à jour la capacité restante
        event.setCapaciteRestante(event.getCapaciteRestante() - nombrePlaces);
        eventRepository.save(event);

        return reservation;
    }

    // 2. Confirmation d'une réservation
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));

        if (reservation.getStatut() != ReservationStatus.EN_ATTENTE) {
            throw new IllegalArgumentException("Seule une réservation en attente peut être confirmée");
        }

        reservation.setStatut(ReservationStatus.CONFIRMEE);
        reservation.setEmailConfirmationEnvoye(true);

        return reservationRepository.save(reservation);
    }

    // 3. Annulation d'une réservation
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));

        // Vérifier que l'annulation est possible
        if (reservation.getStatut() == ReservationStatus.ANNULEE) {
            throw new IllegalArgumentException("Cette réservation est déjà annulée");
        }

        // Vérifier le délai d'annulation (48h avant l'événement)
        Event event = reservation.getEvenement();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancelDeadline = event.getDateDebut().minusHours(CANCELLATION_HOURS_BEFORE);

        if (now.isAfter(cancelDeadline)) {
            throw new IllegalArgumentException(
                    "Les annulations ne sont possibles que " + CANCELLATION_HOURS_BEFORE +
                            "h avant l'événement");
        }

        // Annuler la réservation
        reservation.setStatut(ReservationStatus.ANNULEE);
        reservationRepository.save(reservation);

        // Libérer les places
        event.setCapaciteRestante(event.getCapaciteRestante() + reservation.getNombrePlaces());
        eventRepository.save(event);

        return reservation;
    }

    // 4. Récupération des réservations d'un utilisateur
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUtilisateurId(userId);
    }

    // 5. Vérification d'une réservation par code
    public Reservation getReservationByCode(String codeReservation) {
        return reservationRepository.findByCodeReservation(codeReservation)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));
    }

    // 6. Génération d'un récapitulatif de réservation
    public ReservationSummaryDTO getReservationSummary(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));

        return ReservationSummaryDTO.builder()
                .reservationId(reservation.getId())
                .codeReservation(reservation.getCodeReservation())
                .eventTitle(reservation.getEvenement().getTitre())
                .eventDate(reservation.getEvenement().getDateDebut())
                .nombrePlaces(reservation.getNombrePlaces())
                .prixUnitaire(reservation.getEvenement().getPrixUnitaire())
                .montantTotal(reservation.getMontantTotal())
                .statut(reservation.getStatut())
                .dateReservation(reservation.getDateReservation())
                .build();
    }

    // 7. Calcul des statistiques de réservation
    public ReservationStatisticsDTO getReservationStatistics(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUtilisateurId(userId);

        long confirmatedCount = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .count();

        long cancelledCount = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.ANNULEA)
                .count();

        BigDecimal totalSpent = reservationRepository
                .getTotalReservationAmountByUser(userId);

        BigDecimal averageSpent = reservationRepository
                .getAverageReservationAmountByUser(userId);

        return ReservationStatisticsDTO.builder()
                .totalReservations(reservations.size())
                .confirmedReservations(confirmatedCount)
                .cancelledReservations(cancelledCount)
                .totalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO)
                .averageSpent(averageSpent != null ? averageSpent : BigDecimal.ZERO)
                .build();
    }

    // Bonus : Générer le code de réservation au format EVT-XXXXX
    private String generateReservationCode() {
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000);
        String code = "EVT-" + randomNumber;

        // Vérifier l'unicité
        while (reservationRepository.findByCodeReservation(code).isPresent()) {
            randomNumber = 10000 + random.nextInt(90000);
            code = "EVT-" + randomNumber;
        }

        return code;
    }
}