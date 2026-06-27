package com.paul.jobtrackerapi.repositories;

import com.paul.jobtrackerapi.entities.JobApplication;
import com.paul.jobtrackerapi.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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


}
