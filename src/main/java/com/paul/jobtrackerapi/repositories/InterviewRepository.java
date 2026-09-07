package com.paul.jobtrackerapi.repositories;

import com.paul.jobtrackerapi.entities.Interview;
import com.paul.jobtrackerapi.projections.InterviewOutcomeCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByJobApplicationIdOrderByScheduledAtAsc(Long jobApplicationId);

    @Query("""
    SELECT i.outcome AS outcome, COUNT(i) AS count
    FROM Interview i
    WHERE i.jobApplication.user.id = :userId
    GROUP BY i.outcome
    ORDER BY COUNT(i) DESC
""")
    List<InterviewOutcomeCountProjection> countByOutcomeForUser(
            @Param("userId") Long userId
    );

    List<Interview> findByJobApplicationUserIdAndScheduledAtAfterOrderByScheduledAtAsc(
            Long userId,
            LocalDateTime now
    );
}
