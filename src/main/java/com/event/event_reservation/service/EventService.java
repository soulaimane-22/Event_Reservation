package com.event.event_reservation.service;

import com.event.event_reservation.dto.EventOrganizerStatisticsDTO;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.entity.enums.ReservationStatus;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.repository.UserRepository;
import com.event.event_reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    // 1. Création d'un événement
    public Event createEvent(Long userId, String titre, String description, EventCategory categorie,
                             LocalDateTime dateDebut, LocalDateTime dateFin, String lieu, String ville,
                             Integer capaciteMax, BigDecimal prixUnitaire, Double latitude, Double longitude) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Vérification des droits
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.ORGANIZER) {
            throw new IllegalArgumentException("Seul un ADMIN ou ORGANIZER peut créer un événement");
        }

        // Validations
        if (titre == null || titre.length() < 5 || titre.length() > 100) {
            throw new IllegalArgumentException("Le titre doit contenir entre 5 et 100 caractères");
        }

        if (dateDebut == null || dateDebut.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de début doit être dans le futur");
        }

        if (dateFin == null || dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        if (capaciteMax == null || capaciteMax <= 0) {
            throw new IllegalArgumentException("La capacité maximale doit être supérieure à 0");
        }

        if (prixUnitaire == null || prixUnitaire.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif");
        }

        Event event = Event.builder()
                .titre(titre)
                .description(description)
                .categorie(categorie)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .lieu(lieu)
                .ville(ville)
                .capaciteMax(capaciteMax)
                .capaciteRestante(capaciteMax)
                .prixUnitaire(prixUnitaire)
                .latitude(latitude)
                .longitude(longitude)
                .organisateur(user)
                .statut(EventStatus.BROUILLON)
                .build();

        return eventRepository.save(event);
    }

    // 2. Modification d'un événement
    public Event updateEvent(Long userId, Long eventId, String titre, String description,
                             EventCategory categorie, LocalDateTime dateDebut, LocalDateTime dateFin,
                             String lieu, String ville, Integer capaciteMax, BigDecimal prixUnitaire) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        // Vérification des droits
        if (!event.getOrganisateur().getId().equals(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
            if (user.getRole() != UserRole.ADMIN) {
                throw new IllegalArgumentException("Vous n'avez pas le droit de modifier cet événement");
            }
        }

        // Un événement terminé ne peut pas être modifié
        if (event.getStatut() == EventStatus.TERMINE) {
            throw new IllegalArgumentException("Un événement terminé ne peut pas être modifié");
        }

        if (titre != null) event.setTitre(titre);
        if (description != null) event.setDescription(description);
        if (categorie != null) event.setCategorie(categorie);
        if (dateDebut != null) event.setDateDebut(dateDebut);
        if (dateFin != null) event.setDateFin(dateFin);
        if (lieu != null) event.setLieu(lieu);
        if (ville != null) event.setVille(ville);
        if (capaciteMax != null) event.setCapaciteMax(capaciteMax);
        if (prixUnitaire != null) event.setPrixUnitaire(prixUnitaire);

        return eventRepository.save(event);
    }

    // 3. Publication d'un événement
    public Event publishEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        // Vérification des droits
        if (!event.getOrganisateur().getId().equals(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
            if (user.getRole() != UserRole.ADMIN) {
                throw new IllegalArgumentException("Vous n'avez pas le droit de publier cet événement");
            }
        }

        // Vérification que tous les champs obligatoires sont présents
        if (event.getTitre() == null || event.getDateDebut() == null ||
                event.getDateFin() == null || event.getLieu() == null ||
                event.getVille() == null || event.getCapaciteMax() == null) {
            throw new IllegalArgumentException("L'événement doit avoir toutes les informations requises");
        }

        if (event.getStatut() != EventStatus.BROUILLON) {
            throw new IllegalArgumentException("Seul un événement en brouillon peut être publié");
        }

        event.setStatut(EventStatus.PUBLIE);
        return eventRepository.save(event);
    }

    // 4. Annulation d'un événement
    public Event cancelEvent(Long userId, Long eventId, String raison) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        // Vérification des droits
        if (!event.getOrganisateur().getId().equals(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
            if (user.getRole() != UserRole.ADMIN) {
                throw new IllegalArgumentException("Vous n'avez pas le droit d'annuler cet événement");
            }
        }

        event.setStatut(EventStatus.ANNULE);
        eventRepository.save(event);

        // Annuler toutes les réservations associées
        List<Reservation> reservations = reservationRepository
                .findByEvenementIdAndStatut(eventId, ReservationStatus.CONFIRMEE);

        for (Reservation res : reservations) {
            res.setStatut(ReservationStatus.ANNULEE);
            reservationRepository.save(res);
        }

        return event;
    }

    // 5. Suppression d'un événement
    public void deleteEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        // Vérification des droits
        if (!event.getOrganisateur().getId().equals(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
            if (user.getRole() != UserRole.ADMIN) {
                throw new IllegalArgumentException("Vous n'avez pas le droit de supprimer cet événement");
            }
        }

        // Un événement ne peut être supprimé que s'il n'a pas de réservations
        long reservationCount = reservationRepository.countByEvenementId(eventId);
        if (reservationCount > 0) {
            throw new IllegalArgumentException("Un événement avec des réservations ne peut pas être supprimé");
        }

        eventRepository.deleteById(eventId);
    }

    // 6. Recherche d'événements avec filtres multiples
    public List<Event> searchEvents(String ville, EventCategory categorie, LocalDateTime dateDebut,
                                    LocalDateTime dateFin, BigDecimal prixMin, BigDecimal prixMax) {

        List<Event> events = eventRepository.findByStatut(EventStatus.PUBLIE);

        // Filtrer par ville
        if (ville != null && !ville.isEmpty()) {
            events = events.stream()
                    .filter(e -> e.getVille().equalsIgnoreCase(ville))
                    .collect(Collectors.toList());
        }

        // Filtrer par catégorie
        if (categorie != null) {
            events = events.stream()
                    .filter(e -> e.getCategorie() == categorie)
                    .collect(Collectors.toList());
        }

        // Filtrer par date
        if (dateDebut != null && dateFin != null) {
            events = events.stream()
                    .filter(e -> !e.getDateDebut().isBefore(dateDebut) && !e.getDateFin().isAfter(dateFin))
                    .collect(Collectors.toList());
        }

        // Filtrer par prix
        if (prixMin != null && prixMax != null) {
            events = events.stream()
                    .filter(e -> e.getPrixUnitaire().compareTo(prixMin) >= 0 &&
                            e.getPrixUnitaire().compareTo(prixMax) <= 0)
                    .collect(Collectors.toList());
        }

        return events;
    }

    // 7. Calcul des places disponibles
    public Integer getAvailablePlaces(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        return event.getCapaciteRestante();
    }

    // 8. Événements populaires (les plus réservés)
    public List<Event> getPopularEvents(int limit) {
        List<Event> publishedEvents = eventRepository.findByStatut(EventStatus.PUBLIE);

        return publishedEvents.stream()
                .sorted((e1, e2) -> Long.compare(
                        reservationRepository.countByEvenementId(e2.getId()),
                        reservationRepository.countByEvenementId(e1.getId())
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }
    // Génération de statistiques par organisateur
    public EventOrganizerStatisticsDTO getOrganizerStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Vérifier que c'est un ORGANIZER ou ADMIN
        if (user.getRole() != UserRole.ORGANIZER && user.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Seul un ORGANIZER peut accéder à ses statistiques");
        }

        List<Event> events = eventRepository.findByOrganisateurId(userId);

        long totalEvents = events.size();
        long publishedEvents = events.stream()
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .count();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalReservations = 0;

        for (Event event : events) {
            List<Reservation> reservations = reservationRepository
                    .findByEvenementIdAndStatut(event.getId(), ReservationStatus.CONFIRMEE);

            totalReservations += reservations.size();

            for (Reservation res : reservations) {
                totalRevenue = totalRevenue.add(res.getMontantTotal());
            }
        }

        return EventOrganizerStatisticsDTO.builder()
                .totalEvents(totalEvents)
                .publishedEvents(publishedEvents)
                .draftEvents(events.stream().filter(e -> e.getStatut() == EventStatus.BROUILLON).count())
                .totalReservations(totalReservations)
                .totalRevenue(totalRevenue)
                .build();
    }

    // 9. Vérification automatique des événements terminés
    @Transactional
    public void checkAndUpdateTerminatedEvents() {
        List<Event> publishedEvents = eventRepository.findByStatut(EventStatus.PUBLIE);
        LocalDateTime now = LocalDateTime.now();

        for (Event event : publishedEvents) {
            if (event.getDateFin().isBefore(now)) {
                event.setStatut(EventStatus.TERMINE);
                eventRepository.save(event);
            }
        }
    }
}