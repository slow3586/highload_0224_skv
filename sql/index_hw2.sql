create extension pg_trgm;
CREATE INDEX users_name_idx ON users USING GIN (first_name gin_trgm_ops, second_name gin_trgm_ops);