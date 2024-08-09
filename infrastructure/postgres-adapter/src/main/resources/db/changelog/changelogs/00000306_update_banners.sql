DELETE FROM banners_closed_by;
DELETE FROM banners;

ALTER TABLE banners ADD COLUMN long_description TEXT NOT NULL;
ALTER TABLE banners ADD COLUMN title TEXT NOT NULL;
ALTER TABLE banners ADD COLUMN sub_title TEXT NOT NULL;
ALTER TABLE banners RENAME COLUMN text TO short_description;
ALTER TABLE banners ADD COLUMN date TIMESTAMP;
