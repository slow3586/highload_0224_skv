    CREATE TABLE IF NOT EXISTS users
(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    first_name character varying(1000),
    second_name character varying(1000),
    birthdate date,
    biography character varying(1000),
    city character varying(1000),
    password character varying
)