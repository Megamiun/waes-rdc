CREATE TYPE card_type AS ENUM ('CREDIT', 'DEBIT');

CREATE TABLE account_card
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id       UUID REFERENCES account                    NOT NULL,
    holder_name      VARCHAR(150)                               NOT NULL,
    -- For simplicity, we are not going to be PCI-DSS compliant here
    pan              VARCHAR(20)                                NOT NULL UNIQUE,
    cvv              VARCHAR(3)                                 NOT NULL,
    expiration_month INT                                        NOT NULL,
    expiration_year  INT                                        NOT NULL,
    card_limit       NUMERIC(15, 2),
    type             CARD_TYPE                                  NOT NULL,
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_account_card_pan ON account_card (pan);