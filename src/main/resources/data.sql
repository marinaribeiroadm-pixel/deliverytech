-- ============================================================
--  CLIENTES
-- ============================================================
INSERT INTO cliente (nome, email, telefone, endereco, ativo) VALUES
('Ana Souza',        'ana.souza@email.com',        '(11) 91234-5678', 'Rua das Flores, 120 - São Paulo/SP',    TRUE),
('Bruno Lima',       'bruno.lima@email.com',       '(11) 92345-6789', 'Av. Paulista, 1500 - São Paulo/SP',     TRUE),
('Carla Mendes',     'carla.mendes@email.com',     '(21) 93456-7890', 'Rua da Praia, 45 - Rio de Janeiro/RJ',  TRUE),
('Diego Ferreira',   'diego.ferreira@email.com',   '(31) 94567-8901', 'Rua Minas, 300 - Belo Horizonte/MG',    TRUE),
('Elisa Rocha',      'elisa.rocha@email.com',      '(41) 95678-9012', 'Av. Batel, 900 - Curitiba/PR',          FALSE),
('Felipe Almeida',   'felipe.almeida@email.com',   '(51) 96789-0123', 'Rua dos Andradas, 250 - Porto Alegre/RS', TRUE);

-- ============================================================
--  RESTAURANTES
-- ============================================================
INSERT INTO restaurante (nome, categoria, endereco, telefone, taxa_entrega, ativo, avaliacao) VALUES
('Sabor Mineiro',   'Brasileira',   'Rua das Acácias, 10 - São Paulo/SP',   '(11) 3000-1111', 6.50,  TRUE,  4.7),
('Pizza Nona',      'Italiana',     'Av. Itália, 200 - São Paulo/SP',        '(11) 3000-2222', 8.00,  TRUE,  4.5),
('Sushi Zen',       'Japonesa',     'Rua do Japão, 55 - São Paulo/SP',       '(11) 3000-3333', 12.00, TRUE,  4.8),
('Burger House',    'Hamburgueria', 'Av. Brasil, 1234 - Rio de Janeiro/RJ',  '(21) 3000-4444', 5.00,  TRUE,  4.3),
('Taco Loco',       'Mexicana',     'Rua do México, 88 - Rio de Janeiro/RJ', '(21) 3000-5555', 7.50,  FALSE, 4.0),
('Café da Esquina', 'Cafeteria',    'Rua Central, 12 - Curitiba/PR',         '(41) 3000-6666', 0.00,  TRUE,  4.6);

-- ============================================================
--  PRODUTOS
--  restaurante_id -> ordem de inserção acima (1 a 6)
-- ============================================================
INSERT INTO produto (nome, descricao, preco, categoria, disponivel, restaurante_id) VALUES
-- Sabor Mineiro (id 1)
('Frango com Quiabo',  'Frango caipira com quiabo e angu',        32.90, 'Prato Principal', TRUE,  1),
('Feijão Tropeiro',    'Feijão tropeiro com torresmo',            28.50, 'Prato Principal', TRUE,  1),
('Pão de Queijo (6)',  'Porção de pão de queijo mineiro',         15.00, 'Entrada',         TRUE,  1),
-- Pizza Nona (id 2)
('Pizza Margherita',   'Molho, mussarela, tomate e manjericão',    45.00, 'Pizza',           TRUE,  2),
('Pizza Calabresa',    'Calabresa fatiada, cebola e mussarela',    48.00, 'Pizza',           TRUE,  2),
('Pizza Quatro Queijos','Mussarela, provolone, gorgonzola e parmesão', 52.00, 'Pizza',       TRUE,  2),
-- Sushi Zen (id 3)
('Combo Sushi 20 peças','Sashimi, niguiri e uramaki variados',     89.90, 'Combo',           TRUE,  3),
('Temaki Salmão',      'Cone de alga com salmão e cream cheese',   24.50, 'Temaki',          TRUE,  3),
('Hot Roll (8)',       'Hot roll de salmão empanado',              29.90, 'Entrada',         TRUE,  3),
-- Burger House (id 4)
('X-Burger',           'Pão, carne, queijo e molho especial',      22.00, 'Hambúrguer',      TRUE,  4),
('X-Bacon',            'Pão, carne, queijo, bacon e cebola',       28.00, 'Hambúrguer',      TRUE,  4),
('Batata Frita Média', 'Porção de batata frita crocante',         14.50, 'Acompanhamento',  TRUE,  4),
-- Taco Loco (id 5)
('Taco de Carne',      'Tortilha com carne, queijo e pico de gallo', 18.00, 'Taco',          TRUE,  5),
('Nachos',             'Nachos com queijo cheddar e guacamole',    21.00, 'Entrada',         TRUE,  5),
-- Café da Esquina (id 6)
('Cappuccino',         'Cappuccino tradicional 200ml',             9.50, 'Bebida',           TRUE,  6),
('Croissant',          'Croissant de manteiga',                    8.00, 'Padaria',          TRUE,  6),
('Bolo de Cenoura',    'Fatia de bolo de cenoura com chocolate',   7.50, 'Padaria',          FALSE, 6);

-- ============================================================
--  PEDIDOS
--  Valores de status_pedido devem bater com os nomes do enum.
--  Ajuste se seu StatusPedido tiver outros valores.
-- ============================================================
INSERT INTO pedido (numero_pedido, data_pedido, entrega, subtotal, taxa_entrega, valor_total, status_pedido, cliente_id, restaurante_id) VALUES
('PED-0001', '2026-10-20 12:15:00', TRUE, 32.90, 6.50, 39.40,  'ENTREGUE',   1, 1),
('PED-0002', '2026-10-20 19:40:00', TRUE, 93.00, 8.00, 101.00, 'ENTREGUE',   2, 2),
('PED-0003', '2026-10-21 11:05:00', TRUE, 89.90, 12.00, 101.90,'ENTREGUE',   3, 3),
('PED-0004', '2026-10-22 20:30:00', FALSE, 36.50, 5.00, 41.50, 'PENDENTE',   4, 4),
('PED-0005', '2026-10-23 10:10:00', TRUE, 28.50, 6.50, 35.00,  'CONFIRMADO', 1, 1),
('PED-0006', '2026-10-23 18:55:00', TRUE, 50.00, 8.00, 58.00,  'PREPARANDO', 2, 2),
('PED-0007', '2026-10-23 19:15:00', FALSE, 18.00, 7.50, 25.50, 'CANCELADO',  4, 5),
('PED-0008', '2026-10-23 20:05:00', TRUE, 9.50, 0.00, 9.50,    'PENDENTE',   6, 6);

-- ============================================================
--  ITENS DE PEDIDO
--  preco_total = quantidade * preco_unitario
-- ============================================================
INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario, preco_total) VALUES
-- PED-0001 (Sabor Mineiro)
(1, 1, 1, 32.90, 32.90),

-- PED-0002 (Pizza Nona)
(2, 4, 1, 45.00, 45.00),
(2, 5, 1, 48.00, 48.00),

-- PED-0003 (Sushi Zen)
(3, 7, 1, 89.90, 89.90),

-- PED-0004 (Burger House)
(4, 10, 1, 22.00, 22.00),
(4, 12, 1, 14.50, 14.50),

-- PED-0005 (Sabor Mineiro)
(5, 2, 1, 28.50, 28.50),

-- PED-0006 (Pizza Nona)
(6, 6, 1, 52.00, 52.00),

-- PED-0007 (Taco Loco)
(7, 13, 1, 18.00, 18.00),

-- PED-0008 (Café da Esquina)
(8, 15, 1, 9.50, 9.50);