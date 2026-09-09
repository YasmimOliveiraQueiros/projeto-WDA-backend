CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       phone VARCHAR(20) NOT NULL,
                       cpf VARCHAR(14) NOT NULL,
                       birth_date DATE NOT NULL,
                       address VARCHAR(255) NOT NULL,
                       is_admin BOOLEAN NOT NULL DEFAULT FALSE,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL
);