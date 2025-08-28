-- Zusätzliche Test-Transaktionen für Development
INSERT INTO transactions (bank_name, account_number, booking_date, transaction_type, amount, reference, counterparty, import_source) VALUES
('TEST_BANK', 'DE12345678901234567890', '2024-08-20', 'DEBIT', -120.50, 'EDEKA MARKT TESTSTADT', 'EDEKA', 'TEST_CSV'),
('TEST_BANK', 'DE12345678901234567890', '2024-08-19', 'DEBIT', -55.00, 'ARAL TANKSTELLE', 'ARAL', 'TEST_CSV'),
('TEST_BANK', 'DE12345678901234567890', '2024-08-18', 'CREDIT', 3200.00, 'GEHALT AUGUST 2024', 'TESTFIRMA GMBH', 'TEST_CSV');
