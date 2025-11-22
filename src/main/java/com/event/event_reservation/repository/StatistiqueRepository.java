package com.event.event_reservation.repository;

import com.event.event_reservation.entity.Statistique;
import com.event.event_reservation.entity.enums.StatisticType;
import com.event.event_reservation.entity.enums.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StatistiqueRepository extends JpaRepository<Statistique, Long> {

    // Trouver les statistiques par période
    List<Statistique> findByPeriode(String periode);

    // Trouver les statistiques par type
    List<Statistique> findByType(StatisticType type);

    // Trouver les statistiques par type et période
    List<Statistique> findByTypeAndPeriode(StatisticType type, String periode);

    // Trouver les statistiques par catégorie
    List<Statistique> findByCategorie(EventCategory categorie);

    // Trouver les statistiques d'une période pour un type donné
    @Query("SELECT s FROM Statistique s WHERE s.periode = :periode AND s.type = :type")
    List<Statistique> findStatisticsByPeriodAndType(
            @Param("periode") String periode,
            @Param("type") StatisticType type
    );

    // Trouver les 12 dernières périodes de statistiques pour un type donné
    @Query("SELECT s FROM Statistique s WHERE s.type = :type ORDER BY s.periode DESC LIMIT 12")
    List<Statistique> findLast12MonthsStatistics(@Param("type") StatisticType type);
}