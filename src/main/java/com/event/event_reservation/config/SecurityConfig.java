package com.event.event_reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // ========== Pages PUBLIQUES (sans authentification) ==========
                        .requestMatchers("/", "/login", "/register").permitAll()
                        .requestMatchers("/events", "/event/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // ========== Ressources Vaadin (CSS, JS, images) ==========
                        .requestMatchers("/VAADIN/**", "/frontend/**", "/images/**", "/styles/**").permitAll()
                        .requestMatchers("/sw.js", "/offline.html", "/icons/**", "/line-awesome/**").permitAll()

                        // ========== Pages CLIENT (CLIENT, ORGANIZER, ADMIN) ==========
                        .requestMatchers("/dashboard", "/my-reservations", "/profile", "/event/*/reserve")
                        .hasAnyRole("CLIENT", "ORGANIZER", "ADMIN")

                        // ========== Pages ORGANIZER (ORGANIZER, ADMIN) ==========
                        .requestMatchers("/organizer/**")
                        .hasAnyRole("ORGANIZER", "ADMIN")

                        // ========== Pages ADMIN ==========
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // ========== Tout le reste nécessite authentification ==========
                        .anyRequest().authenticated()
                )

                // ========== CSRF Configuration ==========
                // Vaadin gère CSRF lui-même, donc on désactive pour Vaadin
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                        .ignoringRequestMatchers("/VAADIN/**")
                )

                // ========== Headers Configuration (pour H2 Console) ==========
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )

                // ========== Login Configuration ==========
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                // ========== Logout Configuration ==========
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}