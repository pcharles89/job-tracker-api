ALTER TABLE job_applications
    ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE job_applications
    ADD CONSTRAINT fk_job_applications_user
        FOREIGN KEY (user_id) REFERENCES users(id);