ALTER TYPE document_type ADD VALUE 'PASSPORT';

ALTER TABLE account
    ALTER COLUMN name SET NOT NULL;
ALTER TABLE account
    ALTER COLUMN surname SET NOT NULL;
ALTER TABLE account
    ADD COLUMN created_at TIMESTAMP
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL;
-- Skipping updated_at for simplicity

ALTER TABLE account_document
    ALTER COLUMN account_id SET NOT NULL;
ALTER TABLE account_document
    ALTER COLUMN number SET NOT NULL;
ALTER TABLE account_document
    ALTER COLUMN type SET NOT NULL;
ALTER TABLE account_document
    ADD COLUMN created_at TIMESTAMP
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL;


CREATE OR REPLACE FUNCTION set_created_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_account_created_at
    BEFORE INSERT ON account
    FOR EACH ROW EXECUTE FUNCTION set_created_at();

CREATE TRIGGER update_account_document_created_at
    BEFORE INSERT ON account_document
    FOR EACH ROW EXECUTE FUNCTION set_created_at();