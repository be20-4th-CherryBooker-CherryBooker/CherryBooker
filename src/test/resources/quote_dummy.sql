DELETE FROM quote;

INSERT INTO quote (quote_id, user_id, user_book_id, content, book_title, author, image_path, comment, created_at)
VALUES (1, 1, 100, '기존 글귀입니다.', '아몬드', '손원평', null, null, NOW());
