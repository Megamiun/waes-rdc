CREATE EXTENSION "uuid-ossp";

ALTER TABLE account ALTER COLUMN id SET DEFAULT uuid_generate_v4();
ALTER TABLE account_document ALTER COLUMN id SET DEFAULT uuid_generate_v4();
ALTER TABLE transaction ALTER COLUMN id SET DEFAULT uuid_generate_v4();
ALTER TABLE ledger_entry ALTER COLUMN id SET DEFAULT uuid_generate_v4();

DROP TRIGGER update_transaction_updated_at ON transaction;
CREATE TRIGGER update_transaction_updated_at
    BEFORE INSERT OR UPDATE ON transaction
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER update_transaction_created_at
    BEFORE INSERT ON transaction
    FOR EACH ROW EXECUTE FUNCTION set_created_at();

CREATE TRIGGER update_ledger_entry_created_at
    BEFORE INSERT ON ledger_entry
    FOR EACH ROW EXECUTE FUNCTION set_created_at();