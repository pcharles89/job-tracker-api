package com.paul.jobtrackerapi.repositories;

import com.paul.jobtrackerapi.entities.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationStatusHistoryRepository
        extends JpaRepository<ApplicationStatusHistory, Long> {

    List<ApplicationStatusHistory>
    findByJobApplicationIdOrderByChangedAtAsc(Long jobApplicationId);
}
