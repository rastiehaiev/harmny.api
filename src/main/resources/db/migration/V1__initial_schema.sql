SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';

SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';
SET default_with_oids = false;

CREATE SCHEMA IF NOT EXISTS public;

CREATE TABLE IF NOT EXISTS activity
(
    id                    TEXT                  NOT NULL,
    user_id               TEXT                  NOT NULL,
    application_id        TEXT    DEFAULT NULL,
    parent_activity_id    TEXT    DEFAULT NULL REFERENCES activity (id) ON DELETE CASCADE,
    "name"                TEXT                  NOT NULL,
    is_group              BOOLEAN DEFAULT FALSE NOT NULL,
    created_at            TIMESTAMPTZ           NOT NULL,
    last_updated_at       TIMESTAMPTZ           NOT NULL,
    current_repetition_id TEXT    DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_name
        UNIQUE (user_id, parent_activity_id, "name")
);

CREATE TABLE IF NOT EXISTS activity_repetition
(
    id             TEXT                 NOT NULL,
    activity_id    TEXT                 NOT NULL REFERENCES activity (id) ON DELETE CASCADE,
    application_id TEXT    DEFAULT NULL,
    calories_burnt INT     DEFAULT NULL,
    count          INT     DEFAULT NULL,
    distance       INT     DEFAULT NULL,
    pain_level     INT     DEFAULT NULL,
    heart_rate     INT     DEFAULT NULL,
    mood           INT     DEFAULT NULL,
    time_spent_ms  INT     DEFAULT NULL,
    created_at     TIMESTAMPTZ          NOT NULL,
    completed      BOOLEAN DEFAULT TRUE NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE activity
    ADD CONSTRAINT fk_activity_repetition_current_repetition_id FOREIGN KEY (current_repetition_id) REFERENCES activity_repetition (id);

CREATE INDEX activity_repetition_activity_id_application_id_idx ON activity_repetition (activity_id, application_id);
