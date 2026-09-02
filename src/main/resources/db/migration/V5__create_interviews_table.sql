CREATE TABLE interviews (
        id BIGSERIAL PRIMARY KEY,
        job_application_id BIGINT NOT NULL,
        type VARCHAR(50) NOT NULL,
        scheduled_at TIMESTAMP NOT NULL,
        notes TEXT,
        outcome VARCHAR(50) NOT NULL,

        CONSTRAINT fk_interview_job_application
        FOREIGN KEY (job_application_id)
        REFERENCES job_applications(id)
        ON DELETE CASCADE
);