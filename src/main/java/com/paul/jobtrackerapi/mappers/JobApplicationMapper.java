package com.paul.jobtrackerapi.mappers;

import com.paul.jobtrackerapi.dtos.CreateJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.JobApplicationResponse;
import com.paul.jobtrackerapi.entities.JobApplication;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobApplicationMapper {

    JobApplication toEntity(CreateJobApplicationRequest request);

    JobApplicationResponse toResponse(JobApplication jobApplication);
}
