CREATE TABLE IF NOT EXISTS users
(
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    first_name  character varying(1000),
    second_name character varying(1000),
    birthdate   date,
    biography   character varying(1000),
    city        character varying(1000),
    password    character varying
);

CREATE TABLE IF NOT EXISTS posts
(
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    author_user_id uuid                                       NOT NULL,
    date_created   date                                       NOT NULL,
    text           character varying(1000)                    NOT NULL
);

CREATE TABLE IF NOT EXISTS friendships
(
    id        uuid PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    user_id   uuid                                       NOT NULL,
    friend_id uuid                                       NOT NULL
);