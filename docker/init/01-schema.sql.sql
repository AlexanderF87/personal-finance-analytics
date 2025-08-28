-- 🏦 Personal Finance Analytics Platform - PostgreSQL Schema
-- Production-ready database schema for German banking data

-- Drop existing tables (development only)
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS categories CASCADE;

-- Create Categories table (Composite Pattern: Parent-Child structure)
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    color_hex VARCHAR(7) DEFAULT '#6C5CE7',
    icon VARCHAR(10),
    parent_category_id BIGINT REFERENCES categories(id),
    keywords TEXT,
    is_expense BOOLEAN DEFAULT true,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50),
    booking_date DATE NOT NULL,
    value_date DATE,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEBIT', 'CREDIT')),
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'EUR',
    reference VARCHAR(500),
    counterparty VARCHAR(200),
    category_id BIGINT REFERENCES categories(id),
    processing_state VARCHAR(20) DEFAULT 'PENDING' CHECK (processing_state IN ('PENDING', 'PROCESSED', 'FAILED', 'CANCELLED')),
    import_source VARCHAR(20),
    import_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    raw_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance optimization
CREATE INDEX idx_transactions_booking_date ON transactions(booking_date);
CREATE INDEX idx_transactions_bank_name ON transactions(bank_name);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_state ON transactions(processing_state);
CREATE INDEX idx_transactions_amount ON transactions(amount);
CREATE INDEX idx_categories_parent ON categories(parent_category_id);
CREATE INDEX idx_categories_active ON categories(is_active);

-- Insert default German banking categories
INSERT INTO categories (name, display_name, icon, keywords, is_expense, color_hex) VALUES
-- Main Income Categories
('salary', '💰 Gehalt & Lohn', '💰', 'gehalt,lohn,salary,bezahlung,arbeitgeber', false, '#2ECC71'),
('investment_income', '📈 Kapitalerträge', '📈', 'dividende,zinsen,investment,aktien,fonds', false, '#3498DB'),
('other_income', '💸 Sonstige Einnahmen', '💸', 'erstattung,rückzahlung,bonus,geschenk', false, '#9B59B6'),

-- Main Expense Categories
('groceries', '🛒 Lebensmittel & Restaurants', '🛒', 'rewe,edeka,aldi,lidl,kaufland,restaurant,mcdonald,burger', true, '#E74C3C'),
('transport', '🚗 Transport & Mobilität', '🚗', 'tankstelle,shell,aral,db,deutsche bahn,uber,taxi,öpnv', true, '#F39C12'),
('housing', '🏠 Wohnen & Nebenkosten', '🏠', 'miete,strom,gas,wasser,heizung,internet,telefon', true, '#34495E'),
('healthcare', '🏥 Gesundheit & Medizin', '🏥', 'apotheke,arzt,krankenhaus,medikament,zahnarzt', true, '#E67E22'),
('entertainment', '🎬 Unterhaltung & Freizeit', '🎬', 'kino,netflix,spotify,amazon,entertainment,hobby', true, '#9B59B6'),
('shopping', '🛍️ Shopping & Kleidung', '🛍️', 'amazon,zalando,h&m,kleidung,schuhe,online', true, '#FF6B9D'),
('insurance', '🛡️ Versicherungen', '🛡️', 'versicherung,haftpflicht,krankenversicherung,auto', true, '#1ABC9C'),
('education', '📚 Bildung & Weiterbildung', '📚', 'studium,kurs,buch,seminar,weiterbildung,schule', true, '#3498DB'),
('financial', '🏦 Finanzdienstleistungen', '🏦', 'bank,gebühr,kontoführung,kreditkarte,kredit', true, '#95A5A6'),
('uncategorized', '❓ Unkategorisiert', '❓', '', true, '#BDC3C7');

-- Insert subcategories for groceries
INSERT INTO categories (name, display_name, icon, parent_category_id, keywords, is_expense, color_hex) VALUES
('supermarket', '🏪 Supermarkt', '🏪', (SELECT id FROM categories WHERE name = 'groceries'), 'rewe,edeka,aldi,lidl,kaufland,netto,penny', true, '#E74C3C'),
('restaurants', '🍽️ Restaurants', '🍽️', (SELECT id FROM categories WHERE name = 'groceries'), 'restaurant,pizza,mcdonald,burger king,kfc,lieferando', true, '#FF4757'),
('cafes', '☕ Cafés & Bäckerei', '☕', (SELECT id FROM categories WHERE name = 'groceries'), 'starbucks,café,bäckerei,konditorei,kaffee', true, '#FF6B35');

-- Insert subcategories for transport  
INSERT INTO categories (name, display_name, icon, parent_category_id, keywords, is_expense, color_hex) VALUES
('fuel', '⛽ Kraftstoff', '⛽', (SELECT id FROM categories WHERE name = 'transport'), 'shell,aral,esso,bp,jet,tankstelle', true, '#F39C12'),
('public_transport', '🚌 ÖPNV', '🚌', (SELECT id FROM categories WHERE name = 'transport'), 'db,deutsche bahn,hvv,mvg,öpnv,bus,bahn', true, '#FFA726'),
('car_maintenance', '🔧 Auto Service', '🔧', (SELECT id FROM categories WHERE name = 'transport'), 'werkstatt,reparatur,inspektion,reifen,auto', true, '#FF7043');

-- Test data: Sample transactions for development
INSERT INTO transactions (bank_name, account_number, booking_date, transaction_type, amount, reference, counterparty, import_source) VALUES
('Sparkasse', 'DE89370400440532013000', '2024-01-15', 'DEBIT', -85.42, 'REWE MARKT GMBH BERLIN', 'REWE', 'CSV'),
('DKB', 'DE12500105170648489890', '2024-01-15', 'CREDIT', 2500.00, 'GEHALT JANUAR 2024', 'ARBEITGEBER AG', 'CSV'),
('ING', 'DE89370400440532013000', '2024-01-14', 'DEBIT', -45.20, 'SHELL TANKSTELLE', 'SHELL DEUTSCHLAND', 'CSV'),
('Sparkasse', 'DE89370400440532013000', '2024-01-13', 'DEBIT', -750.00, 'MIETE JANUAR 2024', 'VERMIETUNGSGES. MBH', 'CSV'),
('DKB', 'DE12500105170648489890', '2024-01-12', 'DEBIT', -12.99, 'NETFLIX ABONNEMENT', 'NETFLIX', 'CSV');

-- Update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at
CREATE TRIGGER categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER transactions_updated_at
    BEFORE UPDATE ON transactions  
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

-- Create views for analytics
CREATE VIEW v_monthly_summary AS
SELECT 
    DATE_TRUNC('month', booking_date) as month,
    transaction_type,
    COUNT(*) as transaction_count,
    SUM(ABS(amount)) as total_amount
FROM transactions 
GROUP BY DATE_TRUNC('month', booking_date), transaction_type
ORDER BY month DESC;

CREATE VIEW v_category_summary AS
SELECT 
    c.display_name,
    c.icon,
    c.color_hex,
    COUNT(t.id) as transaction_count,
    SUM(ABS(t.amount)) as total_amount
FROM categories c
LEFT JOIN transactions t ON c.id = t.category_id
WHERE c.is_active = true
GROUP BY c.id, c.display_name, c.icon, c.color_hex
ORDER BY total_amount DESC NULLS LAST;

-- Grant permissions (adjust username as needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO finance_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO finance_user;