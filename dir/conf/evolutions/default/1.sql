# --- !Ups

CREATE TYPE user_role AS ENUM ('Applicant', 'Employee', 'Admin');

CREATE TABLE users (
  id        SERIAL NOT NULL PRIMARY KEY,
  username  TEXT UNIQUE,
  firstname TEXT,
  surname   TEXT,
  password  TEXT,
  email     TEXT UNIQUE,
  "role"    user_role NOT NULL
);

CREATE TABLE sessions (
  id        SERIAL    NOT NULL PRIMARY KEY,
  "user"    INTEGER   NOT NULL REFERENCES users,
  "from"    TIMESTAMP NOT NULL DEFAULT now(),
  refreshed TIMESTAMP NOT NULL DEFAULT now(),
  deleted   BOOLEAN   NOT NULL DEFAULT FALSE
);

CREATE TABLE fields (
  id   SERIAL NOT NULL PRIMARY KEY,
  name TEXT   NOT NULL
);

CREATE TABLE jobs (
  id        SERIAL    NOT NULL PRIMARY KEY,
  field     INTEGER   NOT NULL REFERENCES fields,
  name      TEXT      NOT NULL,
  from_date TIMESTAMP NOT NULL,
  to_date   TIMESTAMP,
  country   TEXT,
  description TEXT    NOT NULL,
  requirement TEXT    NOT NULL
);

CREATE TABLE applications (
  id          SERIAL  NOT NULL PRIMARY KEY,
  "user"      INTEGER NOT NULL REFERENCES users,
  job         INTEGER REFERENCES jobs,
  date        TIMESTAMP,
  description TEXT,
  accepted    BOOLEAN
);

CREATE TABLE availabilities (
  id          SERIAL  NOT NULL PRIMARY KEY,
  application INTEGER NOT NULL REFERENCES applications,
  "from"      DATE,
  "to"        DATE
);

CREATE TABLE competences (
  id   SERIAL NOT NULL PRIMARY KEY,
  name TEXT
);

CREATE TABLE application_competences (
  competence          INTEGER NOT NULL REFERENCES competences,
  years_of_experience FLOAT,
  application         INTEGER NOT NULL REFERENCES applications,
  PRIMARY KEY (competence, application)
);


# --- !Downs
DROP TABLE application_competences;
DROP TABLE competences;
DROP TABLE availabilities;
DROP TABLE applications;
DROP TABLE jobs;
DROP TABLE fields;
DROP TABLE sessions;
DROP TABLE users;
DROP TYPE user_role;