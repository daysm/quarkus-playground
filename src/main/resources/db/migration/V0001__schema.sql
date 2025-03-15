CREATE TABLE author
(
    id         INT PRIMARY KEY,
    last_name  TEXT,
    first_name TEXT
);

CREATE TABLE book
(
    id             INT PRIMARY KEY,
    title          TEXT,
    description    TEXT,
    published_year INT,
    author_id      INT NULL REFERENCES author (id)
);