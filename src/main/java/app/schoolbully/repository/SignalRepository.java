package app.schoolbully.repository;

import app.schoolbully.model.entity.Signal;
import app.schoolbully.model.enums.RecommendedAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SignalRepository extends JpaRepository<Signal, UUID> {
    List<Signal> findAllByOrderByCreatedOnDesc();
    
    @Query("SELECT s FROM Signal s WHERE s.recommendedAction = :action ORDER BY s.urgency DESC, s.seriousnessScore DESC, s.createdOn DESC")
    List<Signal> findByRecommendedActionOrderedByUrgencyAndSeverity(@Param("action") RecommendedAction action);
    
    @Query("SELECT s FROM Signal s WHERE s.authorityNotified = :notified ORDER BY s.urgency DESC, s.seriousnessScore DESC, s.createdOn DESC")
    List<Signal> findByAuthorityNotifiedOrderedByUrgencyAndSeverity(@Param("notified") boolean notified);
    
    @Query("SELECT s FROM Signal s WHERE s.recommendedAction = :action AND s.authorityNotified = :notified ORDER BY s.urgency DESC, s.seriousnessScore DESC, s.createdOn DESC")
    List<Signal> findByRecommendedActionAndAuthorityNotifiedOrderedByUrgencyAndSeverity(
        @Param("action") RecommendedAction action, 
        @Param("notified") boolean notified
    );
}

