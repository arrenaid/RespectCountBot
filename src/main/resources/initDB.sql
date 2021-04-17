DROP TABLE IF EXISTS users, count;

CREATE TABLE users
(
    user_id         INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_id         INT UNIQUE              NOT NULL,
    bot_state       VARCHAR                 NOT NULL,
    username        VARCHAR                         ,
    score           INT
);
CREATE TABLE count
(
    count_id      INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    score           INT                     NOT NULL,
    chat_id         INT                     NOT NULL,
    score_name      VARCHAR                 NOT NULL,
    number_changes  INT
);
