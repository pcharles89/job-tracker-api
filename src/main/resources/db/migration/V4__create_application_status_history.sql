CREATE TABLE application_status_history (
        id BIGSERIAL PRIMARY KEY,
        job_application_id BIGINT NOT NULL,
        status VARCHAR(50) NOT NULL,
        changed_at TIMESTAMP NOT NULL,

        CONSTRAINT fk_status_history_job_application
        FOREIGN KEY (job_application_id)
        REFERENCES job_applications(id)
        ON DELETE CASCADE
);