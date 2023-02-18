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

CREATE TABLE IF NOT EXISTS public.activities
(
    id                 TEXT                  NOT NULL,
    user_id            TEXT                  NOT NULL,
    application_id     TEXT    DEFAULT NULL,
    parent_activity_id TEXT    DEFAULT NULL,
    "name"             TEXT                  NOT NULL,
    is_group           BOOLEAN DEFAULT FALSE NOT NULL,
    created_at         TIMESTAMPTZ           NOT NULL,
    last_updated_at    TIMESTAMPTZ           NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_parent_activity_id
        FOREIGN KEY (parent_activity_id)
            REFERENCES public.activities (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_name
        UNIQUE (user_id, parent_activity_id, "name")
);

CREATE TABLE IF NOT EXISTS public.books
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