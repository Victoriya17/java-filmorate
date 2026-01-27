SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE RATINGS RESTART IDENTITY;
TRUNCATE TABLE FILMS RESTART IDENTITY;
TRUNCATE TABLE USERS RESTART IDENTITY;
TRUNCATE TABLE GENRES RESTART IDENTITY;
TRUNCATE TABLE FILM_GENRES;
TRUNCATE TABLE FILM_LIKES;
TRUNCATE TABLE FRIENDS;

SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO RATINGS (NAME, DESCRIPTION)
VALUES ('G',      'У фильма нет возрастных ограничений'), 										-- 1
		('PG',    'Детям рекомендуется смотреть фильм с родителями'),							-- 2
		('PG-13', 'Детям до 13 лет просмотр не желателен'), 									-- 3
		('R',     'Лицам до 17 лет просматривать фильм можно только в присутствии взрослого'),  -- 4
		('NC-17', 'Лицам до 18 лет просмотр запрещён'); 										-- 5

INSERT INTO GENRES (NAME)
VALUES ('Комедия'), 		-- 1
		('Драма'), 			-- 2
		('Мультфильм'),		-- 3
		('Триллер'),		-- 4
		('Документальный'), -- 5
		('Боевик');		    -- 6

INSERT INTO FILMS (NAME , DESCRIPTION , RELEASEDATE , DURATION , RATING_ID)
VALUES ('1+1', 'Фильм о дружбе совершенно разных людей', '2012-04-26', 114 , 3),
		('Шоколад', 'Фильм о жизни кондитера', '2000-12-22', 121 , 3),
		('Зверополис', 'Мультфильм про крольчиху, которая хочет стать полицейским', '2016-03-03', 109, 2),
		('Король Лев', 'Мультфильм про львенка, который преодолевает трудности', '1994-06-24', 89, 1);

INSERT INTO USERS (EMAIL , LOGIN , NAME , BIRTHDAY)
VALUES ('green@yandex.ru', 'Green', 'Green', '1994-05-15'), 	    -- 1
		('yellow@yandex.ru', 'Yellow', 'Yellow', '1983-07-22'), 	-- 2
		('white@yandex.ru', 'White', 'White', '1987-12-03'); 	    -- 3

INSERT INTO FILM_LIKES (FILM_ID, USER_ID)
VALUES (1, 1),
		(2, 1), (2, 2), (2, 3),
		(3, 2), (3, 3),
		(4, 1), (4, 2), (4, 3);

INSERT INTO FRIENDS (USER_ID, FRIEND_ID)
VALUES (1, 2), (1, 3),
		(2, 1),
		(3, 1), (3, 2);

INSERT INTO FILM_GENRES (FILM_ID , GENRE_ID)
VALUES (1, 1), (1, 2),
		(2, 1), (2, 2),
		(3, 1), (3, 3),
		(4, 1), (4, 2), (4, 3);
