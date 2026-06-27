ALTER TABLE job_applications
    ADD COLUMN user_id BIGINT;

UPDATE job_applications
SET user_id = (SELECT id FROM users ORDER BY id LIMIT 1)
WHERE user_id IS NULL;

ALTER TABLE job_applications
    MODIFY COLUMN user_id BIGINT NOT NULL;

ALTER TABLE job_applications
    ADD CONSTRAINT fk_job_applications_user
        FOREIGN KEY (user_id) REFERENCES users(id);