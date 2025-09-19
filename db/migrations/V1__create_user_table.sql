-- V1__create_user_table.sql

CREATE TABLE IF NOT EXISTS users (
    uuid CHAR(36) NOT NULL PRIMARY KEY,
    fullname VARCHAR(255) NOT NULL,
    study_level VARCHAR(255) NOT NULL,
    age INT NOT NULL
);
