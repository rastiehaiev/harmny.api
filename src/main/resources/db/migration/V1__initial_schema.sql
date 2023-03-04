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
    time_spent_ms  INT     DEFAULT NULL,
    created_at     TIMESTAMPTZ          NOT NULL,
    completed      BOOLEAN DEFAULT TRUE NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE activity
    ADD CONSTRAINT fk_activity_repetition_current_repetition_id FOREIGN KEY (current_repetition_id) REFERENCES activity_repetition (id);

CREATE INDEX activity_repetition_activity_id_application_id_idx ON activity_repetition (activity_id, application_id);

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
    routine_id  TEXT NOT NULL REFERENCES routine (id) ON DELETE CASCADE,
    activity_id TEXT NOT NULL REFERENCES activity (id) ON DELETE CASCADE,
    idx         INT  NOT NULL,
    note        TEXT DEFAULT NULL,
    PRIMARY KEY (id)
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
    id                   TEXT NOT NULL,
    routine_execution_id TEXT NOT NULL REFERENCES routine_execution (id) ON DELETE CASCADE,
    routine_item_id      TEXT NOT NULL REFERENCES routine_item (id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS routine_execution_item_2_activity_repetition
(
    routine_execution_item_id TEXT NOT NULL REFERENCES routine_execution_item (id) ON DELETE CASCADE,
    activity_repetition_id    TEXT NOT NULL REFERENCES activity_repetition (id) ON DELETE CASCADE
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

-- TEST DATA --

INSERT INTO activity (id, user_id, application_id, parent_activity_id, name, is_group, created_at, last_updated_at)
VALUES ('b3bc2313-d8a3-451f-9947-2bcd120ea585', '9940c5c3-73c2-4b55-9943-949f14685c98', null, null, 'Health', true,
        '2023-02-24 10:01:08.827256 +00:00', '2023-02-24 10:01:08.827256 +00:00'),
       ('62895b95-0cb1-4b52-b5a2-40ff75343d96', '9940c5c3-73c2-4b55-9943-949f14685c98', null, null, 'Music', true,
        '2023-02-24 10:01:13.025891 +00:00', '2023-02-24 10:01:13.025891 +00:00'),
       ('a8dbe573-0a1e-435c-b93e-cf3d97e9212e', '9940c5c3-73c2-4b55-9943-949f14685c98', null,
        'b3bc2313-d8a3-451f-9947-2bcd120ea585', 'Push-Ups', false, '2023-02-24 10:01:41.524752 +00:00',
        '2023-02-24 10:01:41.524752 +00:00'),
       ('034e8acf-bf88-47c8-b178-76b8adabe6ac', '9940c5c3-73c2-4b55-9943-949f14685c98', null,
        'b3bc2313-d8a3-451f-9947-2bcd120ea585', 'Pull-Ups', false, '2023-02-24 10:01:45.991535 +00:00',
        '2023-02-24 10:01:45.991535 +00:00'),
       ('3cbea44a-8e87-4532-9b80-7eeaeac21e1b', '9940c5c3-73c2-4b55-9943-949f14685c98', null,
        'b3bc2313-d8a3-451f-9947-2bcd120ea585', 'Abs', true, '2023-02-24 10:04:24.155277 +00:00',
        '2023-02-24 10:04:24.155277 +00:00'),
       ('cc6fd1ad-c4e6-499e-a608-c43c1c25d51d', '9940c5c3-73c2-4b55-9943-949f14685c98', null,
        '3cbea44a-8e87-4532-9b80-7eeaeac21e1b', 'Alternating Crunch', false, '2023-02-24 10:05:11.331912 +00:00',
        '2023-02-24 10:05:11.331912 +00:00'),
       ('19997075-8725-442a-8e24-e64d2502b291', '9940c5c3-73c2-4b55-9943-949f14685c98', null,
        '3cbea44a-8e87-4532-9b80-7eeaeac21e1b', 'Leg Push', false, '2023-02-24 10:05:17.931163 +00:00',
        '2023-02-24 10:05:17.931163 +00:00'),
       ('7fc551a2-8a2d-4961-903f-f37b58522354', '9940c5c3-73c2-4b55-9943-949f14685c98', null,
        '3cbea44a-8e87-4532-9b80-7eeaeac21e1b', 'Leg Elevation', false, '2023-02-24 10:05:34.067322 +00:00',
        '2023-02-24 10:05:34.067322 +00:00'),
       ('64f9b66a-8c3c-44cd-b7a6-31f995de61fb', '9940c5c3-73c2-4b55-9943-949f14685c98', null,
        '62895b95-0cb1-4b52-b5a2-40ff75343d96', 'Ear Training', false, '2023-02-24 10:05:54.884357 +00:00',
        '2023-02-24 10:05:54.884357 +00:00'),
       ('e704913c-e2f8-4d92-915a-b481f18e22fb', '9940c5c3-73c2-4b55-9943-949f14685c98', null,
        '62895b95-0cb1-4b52-b5a2-40ff75343d96', 'Technique Lessons', true, '2023-02-24 10:06:15.797877 +00:00',
        '2023-02-24 10:06:15.797877 +00:00');

INSERT INTO routine (id, user_id, application_id, name, created_at, last_updated_at)
VALUES ('72e8b57c-36aa-4bf7-ad42-b5a782944176', '9940c5c3-73c2-4b55-9943-949f14685c98', null, 'Routine #1',
        '2023-02-24 10:12:37.385115 +00:00', '2023-02-24 10:12:37.385115 +00:00');

INSERT INTO routine_item (id, routine_id, activity_id, idx, note)
VALUES ('8b075be2-a239-4058-8b9d-08cf3d9c859f', '72e8b57c-36aa-4bf7-ad42-b5a782944176',
        'cc6fd1ad-c4e6-499e-a608-c43c1c25d51d', 0, 'Note #1'),
       ('7488b995-a8ae-472f-acda-15860586d59a', '72e8b57c-36aa-4bf7-ad42-b5a782944176',
        '7fc551a2-8a2d-4961-903f-f37b58522354', 1, 'Note #2'),
       ('223cb10b-8645-4a7b-8e95-a78fcd5d99df', '72e8b57c-36aa-4bf7-ad42-b5a782944176',
        '034e8acf-bf88-47c8-b178-76b8adabe6ac', 2, 'Note #3');
