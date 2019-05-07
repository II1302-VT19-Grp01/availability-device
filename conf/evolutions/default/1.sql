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

CREATE TABLE inputs (
  id SERIAL NOT NULL PRIMARY KEY,
  "message" TEXT
);


# --- !Downs

DROP TABLE inputs;
DROP TABLE fields;
DROP TABLE sessions;
DROP TABLE users;
DROP TYPE user_role;