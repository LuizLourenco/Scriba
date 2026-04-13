-- src/main/resources/db/migration/V1__create_acervo.sql

CREATE TABLE IF NOT EXISTS book (
                                    id          TEXT PRIMARY KEY,          -- UUID como TEXT no SQLite
                                    isbn        TEXT UNIQUE,
                                    title       TEXT NOT NULL,
                                    subtitle    TEXT,
                                    edition     INTEGER DEFAULT 1,
                                    pub_year    INTEGER,
                                    language    TEXT DEFAULT 'pt',
                                    category    TEXT NOT NULL,
                                    location    TEXT,                      -- ex: 'Estante A, Prateleira 3'
                                    created_at  TEXT DEFAULT (datetime('now')),
    updated_at  TEXT DEFAULT (datetime('now'))
    );

CREATE TABLE IF NOT EXISTS book_copy (
                                         id          TEXT PRIMARY KEY,
                                         book_id     TEXT NOT NULL REFERENCES book(id),
    copy_number INTEGER NOT NULL,
    condition   TEXT DEFAULT 'BOM',        -- BOM, REGULAR, DANIFICADO
    available   INTEGER DEFAULT 1,         -- boolean: 1=sim, 0=não
    created_at  TEXT DEFAULT (datetime('now'))
    );

CREATE TABLE IF NOT EXISTS author (
                                      id    TEXT PRIMARY KEY,
                                      name  TEXT NOT NULL,
                                      bio   TEXT
);

CREATE TABLE IF NOT EXISTS book_author (
                                           book_id   TEXT REFERENCES book(id),
    author_id TEXT REFERENCES author(id),
    PRIMARY KEY (book_id, author_id)
    );

-- Índices para buscas frequentes
CREATE INDEX IF NOT EXISTS idx_book_isbn  ON book(isbn);
CREATE INDEX IF NOT EXISTS idx_book_title ON book(title);
