ALTER TABLE users ADD COLUMN role VARCHAR(10) NOT NULL DEFAULT 'USER';

INSERT INTO users (first_name, last_name, username, email, password_hash, role, balance)
VALUES 
    ('Klein', 'Moretti', 'Fool', 'Klein@grocademy.com', '$2a$10$wFxngzgqVtsmQokxhBgdQekFmJgY0T13d6Ul5mEripHDO8cMPPC22', 'ADMIN', 999999999999),
    ('Audrey', 'Hall', 'Justice', 'Audrey@grocademy.com', '$2a$10$zg7WWtniSMx2ooA0UrosseAqjQUcyqY1h7U4UtcStANGx7wuHGft2', 'ADMIN', 999999999999),
    ('Alger', 'Wilson', 'Hanged_Man', 'Alger@grocademy.com', '$2a$10$B.RonKC6nw.4B.XdgjDXz.7fFHUO2kORToMCsIeHNEahYsNofVezS', 'ADMIN', 999999999999)
ON CONFLICT(username) DO NOTHING;