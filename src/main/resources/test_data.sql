WITH document_themes AS (
    SELECT UNNEST(ARRAY[
        'Configuration', 'Troubleshooting', 'Deployment Guide', 'Installation Manual',
        'API Reference', 'Security Best Practices', 'Quick Start Guide',
        'Advanced Settings', 'Maintenance', 'Upgrading Procedures'
    ]) AS theme
),
doc_types AS (
    SELECT UNNEST(ARRAY['admin', 'user', 'install', 'troubleshoot', 'sdk', 'api']) AS type
),
versions AS (
    SELECT UNNEST(ARRAY['10.2.x', '9.1.x', '8.0', '10.3', '7.x', 'Cloud Edition', 'Legacy']) AS version
),
desc_words AS (
    SELECT UNNEST(ARRAY[
        'comprehensive', 'detailed', 'essential', 'quick', 'full',
        'technical', 'standard', 'advanced', 'basic', 'system'
    ]) AS word
)
SELECT
    'INSERT INTO public.ebook (name, cat1_id, cat2_id, desc_text, cover_url, doc_url, doc_type, version, views, likes) VALUES ('
    || quote_literal(
        (SELECT theme FROM document_themes OFFSET floor(random() * 10) LIMIT 1)
        || ' for Avaya Aura® System - '
        || (SELECT type FROM doc_types OFFSET floor(random() * 6) LIMIT 1)
        || ' (' || LPAD(i::text, 4, '0') || ')'
    ) || ', '
    || (floor(random() * 5) + 1)::integer || ', '
    || (floor(random() * 10) + 1)::integer || ', '
    || quote_literal((SELECT word FROM desc_words OFFSET floor(random() * 10) LIMIT 1)) || ', '
    || CASE WHEN random() < 0.1
           THEN quote_literal('https://fakecovers.com/cover_' || i || '.png')
           ELSE 'null'
       END || ', '
    || quote_literal('https://support.avaya.com/css/public/documents/fake_' || i) || ', '
    || quote_literal((SELECT type FROM doc_types OFFSET floor(random() * 6) LIMIT 1)) || ', '
    || quote_literal((SELECT version FROM versions OFFSET floor(random() * 7) LIMIT 1)) || ', '
    || (floor(random() * 4951) + 50)::integer || ', '
    || (floor(random() * 496) + 5)::integer
    || ') ON CONFLICT ON CONSTRAINT uk_ebook_doc_url DO NOTHING;'
FROM generate_series(4, 1003) i;
