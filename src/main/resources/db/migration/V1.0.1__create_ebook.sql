-- src/main/resources/db/migration/V1.0.1__create_ebook.sql

-- 1. Create user table
CREATE TABLE IF NOT EXISTS public.ebook (
    id BIGINT PRIMARY KEY,

    name VARCHAR(100) NOT NULL,
    cat1_id BIGINT,
    cat2_id BIGINT,
    desc_text TEXT,
    cover_url VARCHAR(200),
    doc_url VARCHAR(200) NOT NULL,
    doc_type VARCHAR(20),
    version VARCHAR(20),
    views INT NOT NULL DEFAULT 0,
    likes INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_ebook_doc_url UNIQUE (doc_url)
);

-- 2. Create indexes
CREATE INDEX idx_ebook_cat1_id ON public.ebook (cat1_id);
CREATE INDEX idx_ebook_cat2_id ON public.ebook (cat2_id);
CREATE INDEX idx_ebook_created_at_desc ON public.ebook (created_at);

-- 3. Create timestamp update function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. Create trigger for ebook table
DROP TRIGGER IF EXISTS update_ebook_updated_at ON public.ebook;
CREATE TRIGGER update_ebook_updated_at
    BEFORE UPDATE ON public.ebook
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();