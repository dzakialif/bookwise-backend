CREATE TABLE ratings (
    rating_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id       UUID NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    rating        INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, book_id)
);

CREATE TABLE comments (
    comment_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id       UUID NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    content       TEXT NOT NULL,
    deleted_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ratings_book_id    ON ratings(book_id);
CREATE INDEX idx_comments_book_id   ON comments(book_id);
CREATE INDEX idx_comments_user_id   ON comments(user_id);
CREATE INDEX idx_comments_active
    ON comments(book_id, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE TRIGGER update_ratings_updated_at
    BEFORE UPDATE ON ratings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_comments_updated_at
    BEFORE UPDATE ON comments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
