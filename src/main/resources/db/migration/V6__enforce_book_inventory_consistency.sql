UPDATE books
SET status = CASE
    WHEN quantity > 0 THEN 'AVAILABLE'
    ELSE 'RENTED'
END;

ALTER TABLE books
    ADD CONSTRAINT chk_books_quantity_non_negative
        CHECK (quantity >= 0),
    ADD CONSTRAINT chk_books_status_matches_quantity
        CHECK (
            (quantity > 0 AND status = 'AVAILABLE')
            OR (quantity = 0 AND status = 'RENTED')
        );
