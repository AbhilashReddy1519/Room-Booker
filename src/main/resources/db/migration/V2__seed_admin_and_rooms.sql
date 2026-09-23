-- Seed an admin account so the API is usable immediately after a fresh clone.
-- Password is 'Admin@123' (BCrypt hash below). Rotate this before any real deployment —
-- call this out explicitly in the README under "Demo credentials".
INSERT INTO users (id, name, email, password_hash, role, created_at)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Admin User',
    'admin@roombooker.local',
    '$2a$10$Ao1OF9B8vs61Yrt0FqhtCubkfboYfXRtNXwxVS5K0Qbbh5UNbgabK',
    'ADMIN',
    CURRENT_TIMESTAMP
);

INSERT INTO rooms (id, name, capacity, location, created_at) VALUES
    ('22222222-2222-2222-2222-222222222221', 'Falcon',  10, 'Block A, 2nd Floor', CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'Orion',   6,  'Block A, 3rd Floor', CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222223', 'Zenith', 20, 'Block B, Ground Floor', CURRENT_TIMESTAMP);
