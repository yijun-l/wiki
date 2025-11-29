INSERT INTO public.ebook (name, cat1_id, cat2_id, desc_text, cover_url, doc_url, doc_type, version, views, likes)
VALUES
('Upgrading Avaya Aura® Communication Manager', 1, 1, 'upgrading', null, 'https://support.avaya.com/css/public/documents/101087614', 'admin', '10.2.x', 450, 35) ON CONFLICT ON CONSTRAINT uk_ebook_doc_url DO NOTHING;

INSERT INTO public.ebook (name, cat1_id, cat2_id, desc_text, cover_url, doc_url, doc_type, version, views, likes)
VALUES
('Avaya Aura® Communication Manager SNMP Administration and Reference Guide', 1, 1, 'snmp', null, 'https://support.avaya.com/css/public/documents/101087590', 'admin', '10.2.x', 380, 31) ON CONFLICT ON CONSTRAINT uk_ebook_doc_url DO NOTHING;

INSERT INTO public.ebook (name, cat1_id, cat2_id, desc_text, cover_url, doc_url, doc_type, version, views, likes)
VALUES
('Deploying Avaya Aura® Communication Manager in Virtualized Environment', 1, 1, 'deploy', null, 'https://support.avaya.com/css/public/documents/101087606', 'admin', '10.2.x', 210, 18) ON CONFLICT ON CONSTRAINT uk_ebook_doc_url DO NOTHING;