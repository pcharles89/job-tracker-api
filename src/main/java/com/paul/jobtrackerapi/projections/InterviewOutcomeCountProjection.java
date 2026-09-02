package com.paul.jobtrackerapi.projections;

import com.paul.jobtrackerapi.entities.InterviewOutcome;

public interface InterviewOutcomeCountProjection {

    InterviewOutcome getOutcome();

    Long getCount();
}
