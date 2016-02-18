CREATE TABLE test1 (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    createdat TIMESTAMP NOT NULL DEFAULT now()
);

CREATE SEQUENCE cross_seq START 1;

CREATE TABLE test2 (
    id integer NOT NULL PRIMARY KEY DEFAULT nextval('cross_seq'),
    name TEXT NOT NULL,
    otherName TEXT NOT NULL DEFAULT 'quill'
);