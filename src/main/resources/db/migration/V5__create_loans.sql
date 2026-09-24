CREATE TABLE loans (
                       id BIGSERIAL PRIMARY KEY,
                       user_id BIGINT NOT NULL,
                       book_id BIGINT NOT NULL,
                       loan_date DATE NOT NULL,
                       return_date DATE NOT NULL,
                       returned_at TIMESTAMP,
                       status VARCHAR(20) NOT NULL,
                       observations TEXT,

                       CONSTRAINT fk_loans_user
                           FOREIGN KEY (user_id)
                               REFERENCES users(id),

                       CONSTRAINT fk_loans_book
                           FOREIGN KEY (book_id)
                               REFERENCES books(id)
);