DELETE FROM quote;
DELETE FROM my_lib;
DELETE FROM book;
DELETE FROM users;

INSERT INTO users (user_id, login_type, user_name, user_email, user_password, user_nickname, user_status, user_role, created_at)
VALUES (1, 'GOOGLE', '테스터', 'tester@example.com', 'password', 'tester', 'NORMAL', 'USER', CURRENT_TIMESTAMP);

INSERT INTO book (book_id, title, author, isbn, cover_image_url, created_at)
VALUES (1, '자바의 정석', '남궁성', 'ISBN-001', 'https://covers/1', CURRENT_TIMESTAMP),
       (2, '이펙티브 자바', '조슈아', 'ISBN-002', 'https://covers/2', CURRENT_TIMESTAMP),
       (3, '클린 코드', '로버트', 'ISBN-003', 'https://covers/3', CURRENT_TIMESTAMP);

INSERT INTO my_lib (my_lib_id, user_id, book_id, book_status, created_at)
VALUES (10, 1, 1, 'WISH', TIMESTAMP '2024-01-01 10:00:00'),
       (11, 1, 2, 'READ', TIMESTAMP '2024-01-05 10:00:00');

INSERT INTO quote (quote_id, user_id, user_book_id, content, book_title, author, image_path, comment, status, created_at)
VALUES (100, 1, 11, '멋진 문장', '이펙티브 자바', '조슈아', NULL, '코멘트', 'Y', TIMESTAMP '2024-01-06 09:00:00');
