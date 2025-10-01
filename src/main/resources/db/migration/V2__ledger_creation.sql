CREATE TYPE ledger_entry_type AS ENUM ('DEPOSIT', 'TRANSFER_RECEIVED', 'TRANSFER_SENT', 'WITHDRAWAL', 'FEE');
CREATE TYPE transaction_type AS ENUM ('DEPOSIT', 'TRANSFER', 'WITHDRAWAL');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED');
CREATE TYPE transaction_method AS ENUM ('CREDIT', 'DEBIT', 'ATM');

CREATE TABLE transaction
(
    id         UUID PRIMARY KEY,
    owner_id   UUID REFERENCES account                  NOT NULL,
    amount     NUMERIC(15, 2)                           NOT NULL,
    -- Could also include currency. Will not include for sake of simplicity
    fee_amount NUMERIC(15, 2) DEFAULT 0                 NOT NULL,
    type       TRANSACTION_TYPE                         NOT NULL,
    method     TRANSACTION_METHOD                       NOT NULL,
    status     TRANSACTION_STATUS                       NOT NULL,
    created_at TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL
    -- For sake of simplicity, also not adding for now external_reference, retry_count and other aux fields for async
);

CREATE TABLE transaction_transfer
(
    id          UUID PRIMARY KEY REFERENCES transaction,
    receiver_id UUID REFERENCES account NOT NULL
);

CREATE TABLE ledger_entry
(
    id             UUID PRIMARY KEY,
    account_id     UUID REFERENCES account             NOT NULL,
    transaction_id UUID REFERENCES transaction         NOT NULL,
    amount         NUMERIC(15, 2)                      NOT NULL,
    type           LEDGER_ENTRY_TYPE                   NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_transaction_updated_at
    BEFORE UPDATE ON transaction
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();