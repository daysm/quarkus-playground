DELETE FROM books;
DELETE FROM authors;
INSERT INTO authors (id, last_name, first_name)
VALUES (1, 'Tolkien', 'J.R.R.'),
       (2, 'Lewis', 'C.S'),
       (3, 'Sanderson', 'Brandon'),
       (4, 'Tom', 'Clancy');

INSERT INTO books (id, title, description, published_year, author_id)
VALUES
    -- Source: https://www.tolkiensociety.org/actorModel/books-by-tolkien/
    (1, 'Sir Gawain & The Green Knight', 'A modern translation of the Middle English romance from the stories of King Arthur.', 1925, 1),
    (2, 'The Hobbit: or There and Back Again', 'The bedtime story for his children famously begun on the blank page of an exam script that tells the tale of Bilbo Baggins and the dwarves in their quest to take back the Lonely Mountain from Smaug the dragon.', 1937, 1)
-- ...
;
