CREATE TABLE publishers (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            email VARCHAR(255) NOT NULL,
                            cnpj VARCHAR(18) NOT NULL,
                            city VARCHAR(255) NOT NULL,
                            state VARCHAR(2) NOT NULL,
                            status VARCHAR(20) NOT NULL
);