CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE books (
    book_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title             VARCHAR(255) NOT NULL,
    author_name       VARCHAR(150) NOT NULL,
    category_id       UUID NOT NULL REFERENCES categories(category_id) ON DELETE RESTRICT,
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    description       TEXT,
    isbn              VARCHAR(20) NOT NULL UNIQUE,
    publication_year  INTEGER NOT NULL,
    publisher         VARCHAR(200),
    cover_url         VARCHAR(255),
    file_url          VARCHAR(255),
    total_pages       INTEGER,
    published_at      TIMESTAMPTZ,
    archived_at       TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_books_category_id   ON books(category_id);
CREATE INDEX idx_books_status        ON books(status);
CREATE INDEX idx_books_published_at
    ON books(published_at DESC)
    WHERE status = 'PUBLISHED';
CREATE INDEX idx_books_title_trgm
    ON books USING GIN (title gin_trgm_ops);
CREATE INDEX idx_books_author_name_trgm
    ON books USING GIN (author_name gin_trgm_ops);

CREATE TRIGGER update_books_updated_at
    BEFORE UPDATE ON books
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
