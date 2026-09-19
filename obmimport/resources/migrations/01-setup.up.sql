CREATE TABLE obm_location (
  id TEXT PRIMARY KEY,
  lname TEXT NOT NULL,
  lon REAL NOT NULL,
  lat REAL NOT NULL
);

--;;

CREATE TABLE obm_species (
  id TEXT PRIMARY KEY,
  cname TEXT NOT NULL,
  sname TEXT NOT NULL
);

--;;

CREATE TABLE obm_record (
  id TEXT PRIMARY KEY,
  specy_id TEXT NOT NULL,
  locality_id TEXT NOT NULL,
  record_date TEXT NOT NULL,
  record_count INTEGER
);
