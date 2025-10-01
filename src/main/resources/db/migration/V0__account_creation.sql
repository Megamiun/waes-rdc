CREATE TABLE account
(
    id      UUID PRIMARY KEY,
    name    VARCHAR(128),
    surname VARCHAR(128)
);

CREATE TYPE document_type AS ENUM ('BSN');

CREATE TABLE account_document
(
    id         UUID PRIMARY KEY,
    account_id UUID REFERENCES account,
    number     VARCHAR(32),
    type       DOCUMENT_TYPE
);