INSERT INTO pack_definitions (id, pack_type, card_count, coin_cost, weekly_limit)
VALUES (gen_random_uuid(), 'SCOUT_PACK', 3, 5, NULL),
       (gen_random_uuid(), 'TRANSFER_PACK', 5, 30, 3),
       (gen_random_uuid(), 'GOLDEN_PACK', 3, 150, 1),
       (gen_random_uuid(), 'GAMEWEEK_PACK', 4, 80, 1);