CREATE TABLE IF NOT EXISTS roles
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    UNIQUE (name)
);

INSERT INTO roles (name)
VALUES ('USER');
INSERT INTO roles (name)
VALUES ('ADMIN');

CREATE TABLE IF NOT EXISTS accounts
(
    id       SERIAL PRIMARY KEY,
    name     TEXT NOT NULL,
    email    TEXT NOT NULL,
    password TEXT NOT NULL,
    UNIQUE (email)
);

INSERT INTO accounts (name, email, password)
VALUES ('user', 'user@local.ru', '78fd5dc6bc8b9d7380a732992b184fd85aebdd222247b9b870bdf38862cea4a9');
INSERT INTO accounts (name, email, password)
VALUES ('admin', 'root@local.ru', '78fd5dc6bc8b9d7380a732992b184fd85aebdd222247b9b870bdf38862cea4a9');

CREATE TABLE IF NOT EXISTS account_roles
(
    account_id BIGINT REFERENCES accounts (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    role_id    BIGINT REFERENCES roles (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT account_role_pk PRIMARY KEY (account_id, role_id)
);

INSERT INTO account_roles (account_id, role_id)
VALUES (1, 1);
INSERT INTO account_roles (account_id, role_id)
VALUES (2, 1);
INSERT INTO account_roles (account_id, role_id)
VALUES (2, 2);

CREATE TABLE IF NOT EXISTS places
(
    hall       INT,
    row        INT,
    col        INT,
    CONSTRAINT unique_place UNIQUE (hall, row, col),
    account_id BIGINT REFERENCES accounts (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
    price      INT
);

INSERT INTO places VALUES (1, 1, 1, NULL, 250);
INSERT INTO places VALUES (1, 1, 2, NULL, 250);
INSERT INTO places VALUES (1, 1, 3, NULL, 250);
INSERT INTO places VALUES (1, 1, 4, NULL, 250);
INSERT INTO places VALUES (1, 1, 5, NULL, 250);
INSERT INTO places VALUES (1, 1, 6, NULL, 250);

INSERT INTO places VALUES (1, 2, 1, NULL, 250);
INSERT INTO places VALUES (1, 2, 2, NULL, 250);
INSERT INTO places VALUES (1, 2, 3, NULL, 250);
INSERT INTO places VALUES (1, 2, 4, NULL, 250);
INSERT INTO places VALUES (1, 2, 5, NULL, 250);
INSERT INTO places VALUES (1, 2, 6, NULL, 250);

INSERT INTO places VALUES (1, 3, 1, NULL, 250);
INSERT INTO places VALUES (1, 3, 2, NULL, 250);
INSERT INTO places VALUES (1, 3, 3, NULL, 250);
INSERT INTO places VALUES (1, 3, 4, NULL, 250);
INSERT INTO places VALUES (1, 3, 5, NULL, 250);
INSERT INTO places VALUES (1, 3, 6, NULL, 250);

INSERT INTO places VALUES (1, 4, 1, NULL, 250);
INSERT INTO places VALUES (1, 4, 2, NULL, 250);
INSERT INTO places VALUES (1, 4, 3, NULL, 250);
INSERT INTO places VALUES (1, 4, 4, NULL, 250);
INSERT INTO places VALUES (1, 4, 5, NULL, 250);
INSERT INTO places VALUES (1, 4, 6, NULL, 250);

INSERT INTO places VALUES (1, 5, 1, NULL, 250);
INSERT INTO places VALUES (1, 5, 2, NULL, 250);
INSERT INTO places VALUES (1, 5, 3, NULL, 250);
INSERT INTO places VALUES (1, 5, 4, NULL, 250);
INSERT INTO places VALUES (1, 5, 5, NULL, 250);
INSERT INTO places VALUES (1, 5, 6, NULL, 250);

INSERT INTO places VALUES (1, 6, 1, NULL, 250);
INSERT INTO places VALUES (1, 6, 2, NULL, 250);
INSERT INTO places VALUES (1, 6, 3, NULL, 250);
INSERT INTO places VALUES (1, 6, 4, NULL, 250);
INSERT INTO places VALUES (1, 6, 5, NULL, 250);
INSERT INTO places VALUES (1, 6, 6, NULL, 250);