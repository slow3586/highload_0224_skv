CREATE OR REPLACE FUNCTION posts_update_func()
    RETURNS trigger LANGUAGE plpgsql VOLATILE AS
$BODY$
BEGIN
    PERFORM pg_notify('POSTS_UPDATE', new.author_user_id::text);
    RETURN new;
END
$BODY$;

CREATE OR REPLACE FUNCTION posts_delete_func()
    RETURNS trigger LANGUAGE plpgsql VOLATILE AS
$BODY$
BEGIN
    PERFORM pg_notify('POSTS_UPDATE', old.author_user_id::text);
    RETURN old;
END
$BODY$;

CREATE OR REPLACE TRIGGER posts_insert_trig
    AFTER INSERT ON posts
    REFERENCING NEW TABLE AS new
    FOR EACH ROW
EXECUTE FUNCTION posts_update_func();

CREATE OR REPLACE TRIGGER posts_update_trig
    AFTER UPDATE ON posts
    FOR EACH ROW
EXECUTE FUNCTION posts_update_func();

CREATE OR REPLACE TRIGGER posts_delete_trig
    AFTER DELETE ON posts
    REFERENCING OLD TABLE AS old
    FOR EACH ROW
EXECUTE FUNCTION posts_delete_func();