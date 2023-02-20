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
    id                 TEXT                  NOT NULL,
    user_id            TEXT                  NOT NULL,
    application_id     TEXT    DEFAULT NULL,
    parent_activity_id TEXT    DEFAULT NULL REFERENCES activity (id) ON DELETE CASCADE,
    "name"             TEXT                  NOT NULL,
    is_group           BOOLEAN DEFAULT FALSE NOT NULL,
    created_at         TIMESTAMPTZ           NOT NULL,
    last_updated_at    TIMESTAMPTZ           NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_name
        UNIQUE (user_id, parent_activity_id, "name")
);

CREATE TABLE IF NOT EXISTS activity_occurrence
(
    id             TEXT        NOT NULL,
    activity_id    TEXT        NOT NULL REFERENCES activity (id) ON DELETE CASCADE,
    calories_burnt INT      DEFAULT NULL,
    count          INT      DEFAULT NULL,
    time_spent     INTERVAL DEFAULT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS routine
(
    id              TEXT        NOT NULL,
    user_id         TEXT        NOT NULL,
    application_id  TEXT DEFAULT NULL,
    "name"          TEXT        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    last_updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS routine_item
(
    id          TEXT NOT NULL,
    idx         INT  NOT NULL,
    activity_id TEXT NOT NULL REFERENCES activity (id) ON DELETE CASCADE,
    note        TEXT DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS routine_2_routine_item
(
    routine_id      TEXT NOT NULL REFERENCES routine (id) ON DELETE CASCADE,
    routine_item_id TEXT NOT NULL REFERENCES routine_item (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS routine_execution
(
    id          TEXT        NOT NULL,
    routine_id  TEXT        NOT NULL REFERENCES routine (id) ON DELETE CASCADE,
    started_at  TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ DEFAULT NULL,
    status      TEXT        NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS routine_execution_item
(
    id              TEXT NOT NULL,
    routine_item_id TEXT NOT NULL REFERENCES routine_item (id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS routine_execution_2_routine_execution_item
(
    routine_execution_id      TEXT NOT NULL REFERENCES routine_execution (id) ON DELETE CASCADE,
    routine_execution_item_id TEXT NOT NULL REFERENCES routine_execution_item (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS routine_execution_item_2_activity_occurrence
(
    routine_execution_item_id TEXT NOT NULL REFERENCES routine_execution_item (id) ON DELETE CASCADE,
    activity_occurrence_id    TEXT NOT NULL REFERENCES activity_occurrence (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS books
(
    id                  TEXT        NOT NULL,
    user_id             TEXT        NOT NULL,
    application_id      TEXT        DEFAULT NULL,
    "name"              TEXT        NOT NULL,
    author              TEXT        NOT NULL,
    genre               TEXT        NOT NULL,
    status              TEXT        NOT NULL,
    pages_count         INT         DEFAULT NULL,
    current_page_number INT         DEFAULT NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    last_updated_at     TIMESTAMPTZ NOT NULL,
    started_at          TIMESTAMPTZ DEFAULT NULL,
    finished_at         TIMESTAMPTZ DEFAULT NULL,
    PRIMARY KEY (id)
);
