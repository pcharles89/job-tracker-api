package com.paul.jobtrackerapi.repositories;

import com.paul.jobtrackerapi.entities.JobApplication;
import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.projections.StatusCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {
    Page<JobApplication> findByUser(
            User user,
            Pageable pageable
    );

    Optional<JobApplication> findByIdAndUser(
            Long id,
            User user
    );

    @Query("""
        SELECT j.status AS status, COUNT(j) AS count
        FROM JobApplication j
        WHERE j.user.id = :userId
        GROUP BY j.status
        """)
    List<StatusCountProjection> countApplicationsByStatus(
            @Param("userId") Long userId
    );


}
