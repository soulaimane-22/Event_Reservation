package com.event.event_reservation.service;

import com.event.event_reservation.dto.UserStatisticsDTO;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.UserRepository;
import com.event.event_reservation.repository.ReservationRepository;
import com.event.event_reservation.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. Inscription d'un nouvel utilisateur
    public User registerUser(String nom, String prenom, String email, String password, UserRole role) {
        // Validation
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email ne peut pas être vide");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .actif(true)
                .build();

        return userRepository.save(user);
    }

    // 2. Authentification
    public Optional<User> authenticate(String email, String password) {
        Optional<User> user = userRepository.findByEmailIgnoreCase(email);

        if (user.isPresent() && user.get().getActif() &&
                passwordEncoder.matches(password, user.get().getPassword())) {
            return user;
        }

        return Optional.empty();
    }

    // 3. Mise à jour du profil utilisateur
    public User updateProfile(Long userId, String nom, String prenom, String telephone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        if (nom != null && !nom.trim().isEmpty()) user.setNom(nom);
        if (prenom != null && !prenom.trim().isEmpty()) user.setPrenom(prenom);
        if (telephone != null) user.setTelephone(telephone);

        return userRepository.save(user);
    }

    // 4. Changement de mot de passe
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 8 caractères");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 5. Désactivation/activation d'un compte
    public void toggleUserActive(Long userId, Boolean actif) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        user.setActif(actif);
        userRepository.save(user);
    }

    // 6. Récupération des statistiques utilisateur
    public UserStatisticsDTO getUserStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        long eventsCreated = eventRepository.countByOrganisateurId(userId);
        long reservationsCount = userRepository.findById(userId)
                .map(u -> u.getReservations().size())
                .orElse(0);

        BigDecimal totalSpent = reservationRepository.getTotalReservationAmountByUser(userId);

        return UserStatisticsDTO.builder()
                .userId(userId)
                .eventsCreated(eventsCreated)
                .reservationsCount(reservationsCount)
                .totalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO)
                .build();
    }

    // 7. Liste des utilisateurs avec filtres
    public List<User> getUsersWithFilters(UserRole role, Boolean actif) {
        if (role != null && actif != null) {
            return userRepository.findByRoleAndActifTrue(role);
        }

        if (role != null) {
            return userRepository.findByRole(role);
        }

        if (actif != null && actif) {
            return userRepository.findByActifTrue();
        }

        return userRepository.findAll();
    }

    // Bonus : Recherche utilisateur par nom ou prénom
    public List<User> searchUsers(String search) {
        return userRepository.searchByNomOrPrenom(search);
    }
}