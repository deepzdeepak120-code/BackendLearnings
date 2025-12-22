-- Clean up orphaned expenses (expenses with category_id that doesn't exist in categories)
DELETE FROM expenses WHERE category_id NOT IN (SELECT id FROM categories);
