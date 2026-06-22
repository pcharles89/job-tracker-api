package com.paul.jobtrackerapi.specifications;

import com.paul.jobtrackerapi.entities.ApplicationStatus;
import com.paul.jobtrackerapi.entities.JobApplication;
import org.springframework.data.jpa.domain.Specification;

public class JobApplicationSpecification {
    public static Specification<JobApplication> hasCompanyName(String companyName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("companyName")),
                        "%" + companyName.toLowerCase() + "%"
                );
    }

    public static Specification<JobApplication> hasLocation(String location) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + location.toLowerCase() + "%"
                );
    }

    public static Specification<JobApplication> hasStatus(ApplicationStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }
}
