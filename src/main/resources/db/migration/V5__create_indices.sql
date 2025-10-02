CREATE INDEX idx_account_document_account_id ON account_document(account_id);
CREATE INDEX idx_account_card_account_id ON account_card(account_id);

CREATE INDEX idx_transaction_account_id ON transaction(owner_id);
CREATE INDEX idx_ledger_entry_account_id ON ledger_entry(account_id);
CREATE INDEX idx_ledger_entry_transaction_id ON ledger_entry(transaction_id);