package com.event.event_reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Autoriser librement la console H2 et les ressources nécessaires
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/login", "/VAADIN/**", "/frontend/**").permitAll()
                        .anyRequest().authenticated()
                )

                // CSRF désactivé uniquement pour H2 console
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )

                // Autoriser l'affichage dans des frames pour H2
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )

                // Utiliser form login (page de login) au lieu de HTTP Basic -> pas de popup navigateur
                .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
