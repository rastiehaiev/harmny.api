ALTER TABLE activity_repetition
    ADD COLUMN started_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE activity_repetition
    ADD COLUMN last_started_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE activity_repetition
    ADD COLUMN started BOOLEAN DEFAULT NULL;
ALTER TABLE activity_repetition
    ADD COLUMN restarts INT DEFAULT NULL;
ALTER TABLE activity_repetition
    ADD COLUMN complexity INT DEFAULT NULL;

DROP INDEX activity_repetition_activity_id_application_id_idx;

CREATE INDEX activity_repetition_activity_id_application_id_started_at_idx
    ON activity_repetition (activity_id, application_id, started_at);