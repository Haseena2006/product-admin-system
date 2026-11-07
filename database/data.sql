
INSERT INTO users (username, email, password, role, enabled, created_at) VALUES
('admin', 'admin@example.com', '$2a$10$eM/WsoaqWqHZeKHbCldiuOZ9CmdgdG1obrEwiYEdKZ.4gU3F0rWGK', 'ADMIN', TRUE, NOW()),
('user',  'user@example.com',  '$2a$10$PxoNEerXyy6W8h46jEI9MOlkuwNGCKObcaIrKz4ru4tkbWgVO/czS', 'USER',  TRUE, NOW())
ON CONFLICT DO NOTHING;

INSERT INTO products (name, description, category, price, stock, rating, created_at, updated_at) VALUES
('Wireless Mouse',      'Ergonomic 2.4GHz wireless mouse with silent clicks',      'Electronics',     799.00, 120, 4.4, NOW(), NOW()),
('Mechanical Keyboard', 'Hot-swappable mechanical keyboard with blue switches',    'Electronics',    3499.00,  45, 4.6, NOW(), NOW()),
('27-inch Monitor',     'Full HD IPS monitor with 75Hz refresh rate',              'Electronics',   12999.00,  18, 4.5, NOW(), NOW()),
('Office Chair',        'Ergonomic mesh chair with lumbar support',                'Furniture',      7999.00,  12, 4.5, NOW(), NOW()),
('Clean Code',          'A handbook of agile software craftsmanship',              'Books',           699.00,  55, 4.8, NOW(), NOW()),
('Air Fryer',           '4.2 litre digital air fryer',                             'Home Appliances',5499.00,  14, 4.3, NOW(), NOW())
ON CONFLICT DO NOTHING;
