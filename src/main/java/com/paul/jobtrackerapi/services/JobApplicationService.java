package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.CreateJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.JobApplicationResponse;
import com.paul.jobtrackerapi.dtos.PatchJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.UpdateJobApplicationRequest;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
import com.paul.jobtrackerapi.entities.JobApplication;
import com.paul.jobtrackerapi.exceptions.JobApplicationNotFoundException;
import com.paul.jobtrackerapi.mappers.JobApplicationMapper;
import com.paul.jobtrackerapi.repositories.JobApplicationRepository;
import com.paul.jobtrackerapi.specifications.JobApplicationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final JobApplicationMapper mapper;

    public JobApplicationService(JobApplicationRepository repository,
                                 JobApplicationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public JobApplicationResponse createApplication(CreateJobApplicationRequest request) {
        JobApplication jobApplication = mapper.toEntity(request);
        JobApplication savedApplication = repository.save(jobApplication);

        return mapper.toResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getAllApplications(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getApplicationById(Long id) {
        JobApplication jobApplication = repository.findById(id)
                .orElseThrow(() -> new JobApplicationNotFoundException(id));

        return mapper.toResponse(jobApplication);
    }

    @Transactional
    public JobApplicationResponse updateApplication(Long id, UpdateJobApplicationRequest request) {
        JobApplication existingApplication = repository.findById(id)
                .orElseThrow(() -> new JobApplicationNotFoundException(id));

        existingApplication.setCompanyName(request.getCompanyName());
        existingApplication.setJobTitle(request.getJobTitle());
        existingApplication.setJobUrl(request.getJobUrl());
        existingApplication.setStatus(request.getStatus());
        existingApplication.setAppliedDate(request.getAppliedDate());
        existingApplication.setLocation(request.getLocation());
        existingApplication.setSalaryRange(request.getSalaryRange());
        existingApplication.setNotes(request.getNotes());

        JobApplication savedApplication = repository.save(existingApplication);

        return mapper.toResponse(savedApplication);
    }

    @Transactional
    public void deleteApplication(Long id) {
        if(!repository.existsById(id)) {
            throw new JobApplicationNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> searchApplications(
            String companyName,
            String location,
            ApplicationStatus status,
            Pageable pageable
    ) {

        Specification<JobApplication> spec = Specification.unrestricted();

        if (companyName != null) {
            spec = spec.and(
                    JobApplicationSpecification.hasCompanyName(companyName)
            );
        }

        if (location != null) {
            spec = spec.and(
                    JobApplicationSpecification.hasLocation(location)
            );
        }

        if (status != null) {
            spec = spec.and(
                    JobApplicationSpecification.hasStatus(status)
            );
        }

        return repository.findAll(spec, pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public JobApplicationResponse patchApplication(Long id, PatchJobApplicationRequest request) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> new JobApplicationNotFoundException(id));

        if (request.companyName() != null) {
            application.setCompanyName(request.companyName());
        }

        if (request.jobTitle() != null) {
            application.setJobTitle(request.jobTitle());
        }

        if (request.jobUrl() != null) {
            application.setJobUrl(request.jobUrl());
        }

        if (request.appliedDate() != null) {
            application.setAppliedDate(request.appliedDate());
        }

        if (request.location() != null) {
            application.setLocation(request.location());
        }

        if (request.salaryRange() != null) {
            application.setSalaryRange(request.salaryRange());
        }

        if (request.notes() != null) {
            application.setNotes(request.notes());
        }

        if (request.status() != null) {
            application.setStatus(request.status());
        }

        return mapper.toResponse(application);
    }
}
