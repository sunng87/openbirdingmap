-- sticker path and field-guide text imported from the huashu-bird-sticker
-- dataset (https://github.com/alchaincyf/huashu-bird-sticker)
CREATE TABLE obm_species_ext (
  species_id TEXT PRIMARY KEY,
  -- sticker file path relative to the sticker repo's stickers/ dir
  sticker_path TEXT,
  -- upstream review score of the sticker (see sticker repo data/species.csv)
  sticker_score INTEGER,
  -- zh-Hans field-guide entry as a JSON blob (impression, marks, funFact, ...)
  field_guide TEXT
);
