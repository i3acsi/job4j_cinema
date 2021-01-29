CREATE TABLE IF NOT EXISTS roles
(
    id       SERIAL PRIMARY KEY,
    name     TEXT NOT NULL,
    UNIQUE (name)
);

INSERT INTO roles (name) VALUES ('USER');
INSERT INTO roles (name) VALUES ('ADMIN');

CREATE TABLE IF NOT EXISTS accounts
(
    id       SERIAL PRIMARY KEY,
    name     TEXT NOT NULL,
    email    TEXT NOT NULL,
    password TEXT NOT NULL,
    UNIQUE (email)
);

INSERT INTO accounts (name, email, password) VALUES ('user', 'user@local', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8');
INSERT INTO accounts (name, email, password) VALUES ('admin', 'root@local', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8');

CREATE TABLE IF NOT EXISTS account_roles
(
    account_id BIGINT REFERENCES accounts (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    role_id BIGINT REFERENCES roles (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT account_role_pk PRIMARY KEY (account_id, role_id)
);

INSERT INTO account_roles (account_id, role_id) VALUES (1, 1);
INSERT INTO account_roles (account_id, role_id) VALUES (2, 1);
INSERT INTO account_roles (account_id, role_id) VALUES (2, 2);