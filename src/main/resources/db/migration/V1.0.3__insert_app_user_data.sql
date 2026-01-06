-- src/main/resources/db/migration/V1.0.2__insert_app_user_data.sql

INSERT INTO public.app_user (
    id,
    username,
    email,
    password_hash,
    nickname,
    avatar_url,
    status
) VALUES
(1, 'admin', 'admin@example.com', '$2a$10$adminhash', 'Administrator', NULL, 'active'),
(2, 'alice', 'alice@example.com', '$2a$10$alicehash', 'Alice', NULL, 'active'),
(3, 'bob', 'bob@example.com', '$2a$10$bobhash', 'Bob', NULL, 'active'),
(4, 'charlie', 'charlie@example.com', '$2a$10$charliehash', 'Charlie', NULL, 'disabled'),
(5, 'david', 'david@example.com', '$2a$10$davidhash', 'David', NULL, 'active'),
(6, 'eve', 'eve@example.com', '$2a$10$evehash', 'Eve', NULL, 'locked'),
(7, 'frank', 'frank@example.com', '$2a$10$frankhash', 'Frank', NULL, 'active'),
(8, 'grace', 'grace@example.com', '$2a$10$gracehash', 'Grace', NULL, 'active'),
(9, 'heidi', 'heidi@example.com', '$2a$10$heidihash', 'Heidi', NULL, 'disabled'),
(10, 'ivan', 'ivan@example.com', '$2a$10$ivanhash', 'Ivan', NULL, 'active');