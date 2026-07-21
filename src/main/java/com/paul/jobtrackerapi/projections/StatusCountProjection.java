package com.paul.jobtrackerapi.projections;

import com.paul.jobtrackerapi.entities.ApplicationStatus;

public interface StatusCountProjection {

    ApplicationStatus getStatus();

    long getCount();

}
