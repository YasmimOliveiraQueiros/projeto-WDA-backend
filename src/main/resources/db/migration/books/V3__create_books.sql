CREATE TABLE books (
                       id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       publisher_id BIGINT NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       observations TEXT,

                       CONSTRAINT fk_books_publisher
                           FOREIGN KEY (publisher_id)
                               REFERENCES publishers(id)
);