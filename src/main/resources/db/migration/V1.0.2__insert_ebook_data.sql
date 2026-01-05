-- src/main/resources/db/migration/V1.0.2__insert_ebook_data.sql

INSERT INTO public.ebook (
    id, name, cat1_id, cat2_id, desc_text, cover_url, doc_url, doc_type, version, views, likes
)
SELECT
    (floor(random() * 1000000000000))::bigint,
    themes[ceil(random() * array_length(themes,1))] || ' for Avaya Aura® System - ' ||
    doc_types[ceil(random() * array_length(doc_types,1))] || ' (' || LPAD(i::text, 4, '0') || ')',
    (floor(random() * 5) + 1)::int,
    (floor(random() * 10) + 1)::int,
    words[ceil(random() * array_length(words,1))],
    CASE WHEN random() < 0.1 THEN 'https://fakecovers.com/cover_' || i || '.png' ELSE NULL END,
    'https://support.avaya.com/css/public/documents/fake_' || i,
    doc_types[ceil(random() * array_length(doc_types,1))],
    versions[ceil(random() * array_length(versions,1))],
    (floor(random() * 4951) + 50)::int,
    (floor(random() * 496) + 5)::int
FROM generate_series(1,1000) AS s(i),
LATERAL (SELECT ARRAY[
    'Configuration', 'Troubleshooting', 'Deployment Guide', 'Installation Manual',
    'API Reference', 'Security Best Practices', 'Quick Start Guide',
    'Advanced Settings', 'Maintenance', 'Upgrading Procedures'
] AS themes) AS t1,
LATERAL (SELECT ARRAY['admin','user','install','troubleshoot','sdk','api'] AS doc_types) AS t2,
LATERAL (SELECT ARRAY['10.2.x','9.1.x','8.0','10.3','7.x','Cloud Edition','Legacy'] AS versions) AS t3,
LATERAL (SELECT ARRAY[
    'comprehensive','detailed','essential','quick','full',
    'technical','standard','advanced','basic','system'
] AS words) AS t4
ON CONFLICT ON CONSTRAINT uk_ebook_doc_url DO NOTHING;
