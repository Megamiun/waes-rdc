CREATE TRIGGER update_account_card_created_at
    BEFORE INSERT ON account_card
    FOR EACH ROW EXECUTE FUNCTION set_created_at();