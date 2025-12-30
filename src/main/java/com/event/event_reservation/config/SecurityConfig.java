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
                        // 1. TES PAGES PUBLIQUES (Explicitement autorisées comme demandé)
                        .requestMatchers("/", "/login", "/register").permitAll()
                        .requestMatchers("/events", "/event/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // 2. RESSOURCES VAADIN (Indispensable pour le style et le JS)
                        .requestMatchers("/VAADIN/**", "/frontend/**", "/images/**", "/styles/**").permitAll()
                        .requestMatchers("/sw.js", "/offline.html", "/icons/**", "/line-awesome/**").permitAll()
                        .anyRequest().permitAll()
                )

                // Désactivation CSRF pour éviter les conflits en local
                .csrf(csrf -> csrf.disable())

                // Autoriser l'affichage de la console H2 (frames)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )

                // Configuration du Login (Toujours active pour tes tests d'UI)
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                // Configuration du Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}