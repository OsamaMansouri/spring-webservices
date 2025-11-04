-- Script SQL pour générer des données de test
-- 2 000 catégories et 100 000 produits

-- Nettoyage des données existantes (DÉCOMMENTEZ si vous voulez réinitialiser complètement)
TRUNCATE TABLE item CASCADE;
TRUNCATE TABLE category CASCADE;

-- Réinitialisation des séquences pour que les IDs commencent à 1
-- Vérification et réinitialisation de la séquence category
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'category_id_seq') THEN
        ALTER SEQUENCE category_id_seq RESTART WITH 1;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'item_id_seq') THEN
        ALTER SEQUENCE item_id_seq RESTART WITH 1;
    END IF;
END $$;

-- Génération de 2 000 catégories
-- Utilisation d'une CTE pour garantir que les IDs sont bien séquentiels
WITH category_data AS (
    SELECT 
        'CAT-' || LPAD((ROW_NUMBER() OVER ())::text, 5, '0') as code,
        'Category ' || (ROW_NUMBER() OVER ()) as name,
        NOW() - (RANDOM() * INTERVAL '365 days') as updated_at
    FROM generate_series(1, 2000)
)
INSERT INTO category (code, name, updated_at)
SELECT code, name, updated_at FROM category_data;

-- Vérification que les catégories ont été créées (devrait retourner 2000)
-- SELECT COUNT(*) as categories_created FROM category;

-- Génération de 100 000 produits (environ 50 par catégorie)
-- Utilisation d'une table temporaire pour mapper les IDs de catégories
WITH category_ids AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM category
    ORDER BY id
),
item_data AS (
    SELECT 
        'SKU-' || LPAD((ROW_NUMBER() OVER ())::text, 8, '0') as sku,
        'Item ' || (ROW_NUMBER() OVER ()) as name,
        ROUND((RANDOM() * 9900 + 100)::numeric, 2) as price,  -- Prix entre 100 et 10 000
        (RANDOM() * 1000)::int as stock,                       -- Stock entre 0 et 1000
        (((ROW_NUMBER() OVER () - 1) / 50)::int % (SELECT COUNT(*) FROM category)) + 1 as cat_rn,  -- Index de 1 à 2000
        NOW() - (RANDOM() * INTERVAL '365 days') as updated_at
    FROM generate_series(1, 100000)
)
INSERT INTO item (sku, name, price, stock, category_id, updated_at)
SELECT 
    id.sku,
    id.name,
    id.price,
    id.stock,
    c.id as category_id,  -- Utilise l'ID réel de la catégorie
    id.updated_at
FROM item_data id
JOIN category_ids c ON c.rn = id.cat_rn;

-- Vérification des données générées
SELECT 
    (SELECT COUNT(*) FROM category) as total_categories,
    (SELECT COUNT(*) FROM item) as total_items,
    (SELECT COUNT(DISTINCT category_id) FROM item) as categories_with_items,
    (SELECT AVG(items_per_category) FROM (
        SELECT category_id, COUNT(*) as items_per_category 
        FROM item 
        GROUP BY category_id
    ) sub) as avg_items_per_category;

