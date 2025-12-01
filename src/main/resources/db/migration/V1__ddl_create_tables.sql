CREATE TABLE IF NOT EXISTS person (
                     id BIGSERIAL PRIMARY KEY,
                     name TEXT UNIQUE NOT NULL,
                     mail TEXT UNIQUE NOT NULL,
                     password TEXT NOT NULL
);

COMMENT ON TABLE person IS 'user';
COMMENT ON COLUMN person.id IS 'Id';
COMMENT ON COLUMN person.name IS 'user name';
