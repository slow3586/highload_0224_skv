CREATE USER replicator WITH REPLICATION ENCRYPTED PASSWORD 'replicator';
SELECT pg_create_physical_replication_slot('replication_slot_1');
SELECT pg_create_physical_replication_slot('replication_slot_2');
ALTER SYSTEM SET synchronous_standby_names TO 'ANY 1 (*)';
SELECT pg_reload_conf();