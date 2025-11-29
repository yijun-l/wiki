CREATE TABLE IF NOT EXISTS public.ebook (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    cat1_id BIGINT,
    cat2_id BIGINT,
    desc_text TEXT,
    cover_url VARCHAR(200),
    doc_url VARCHAR(200) NOT NULL,
    doc_type VARCHAR(20),
    version VARCHAR(20),
    views INT DEFAULT 0,
    likes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ebook_doc_url UNIQUE (doc_url)
);