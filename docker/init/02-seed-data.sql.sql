-- Test data for H2 (minimal set for testing)
INSERT INTO categories (name, display_name, icon, keywords, is_expense, color_hex) VALUES
('salary', '💰 Gehalt & Lohn', '💰', 'gehalt,lohn,salary', false, '#2ECC71'),
('groceries', '🛒 Lebensmittel', '🛒', 'rewe,edeka,aldi,lidl', true, '#E74C3C'),
('transport', '🚗 Transport', '🚗', 'tankstelle,shell,aral', true, '#F39C12'),
('uncategorized', '❓ Unkategorisiert', '❓', '', true, '#BDC3C7');

-- Test transactions
INSERT INTO transactions (bank_name, account_number, booking_date, transaction_type, amount, reference, counterparty, import_source) VALUES
('TEST_BANK', 'DE12345678901234567890', '2024-01-15', 'DEBIT', -50.00, 'TEST REWE PURCHASE', 'REWE', 'TEST_CSV'),
('TEST_BANK', 'DE12345678901234567890', '2024-01-15', 'CREDIT', 2000.00, 'TEST SALARY', 'EMPLOYER', 'TEST_CSV'),
('TEST_BANK', 'DE12345678901234567890', '2024-01-14', 'DEBIT', -30.00, 'TEST FUEL', 'SHELL', 'TEST_CSV');