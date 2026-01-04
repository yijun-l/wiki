-- 1. Create user table
CREATE TABLE IF NOT EXISTS public.app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    avatar_url TEXT,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create indexes
CREATE INDEX IF NOT EXISTS idx_app_user_email ON public.app_user(email);
CREATE INDEX IF NOT EXISTS idx_app_user_status ON public.app_user(status);
CREATE INDEX IF NOT EXISTS idx_app_user_created_at ON public.app_user(created_at);

-- 3. Create timestamp update function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. Create trigger for user table
DROP TRIGGER IF EXISTS update_app_user_updated_at ON public.app_user;
CREATE TRIGGER update_app_user_updated_at
    BEFORE UPDATE ON public.app_user
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();