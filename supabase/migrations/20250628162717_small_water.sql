/*
  # Users Table Migration

  1. New Tables
    - `users`
      - `id` (uuid, primary key) - Supabase auth.users ile eşleşir
      - `email` (text, unique, not null) - E-posta adresi
      - `full_name` (text, not null) - Ad soyad
      - `title` (text) - Unvan/pozisyon
      - `website` (text) - Kişisel website
      - `success_story` (text) - Başarı hikayesi
      - `failure_story` (text) - Başarısızlık hikayesi
      - `invite_quota` (integer, default 5) - Yıllık davet hakkı
      - `used_invites` (integer, default 0) - Kullanılan davet sayısı
      - `collaboration_count` (integer, default 0) - İşbirliği sayısı
      - `created_at` (timestamptz, default now())
      - `updated_at` (timestamptz, default now())
      - `is_active` (boolean, default true)

  2. Security
    - Enable RLS on `users` table
    - Users can read their own data
    - Users can update their own profile
    - Public can read basic user info for collaboration
*/

-- Users tablosu
CREATE TABLE IF NOT EXISTS users (
  id uuid PRIMARY KEY,
  email text UNIQUE NOT NULL,
  full_name text NOT NULL,
  title text,
  website text,
  success_story text,
  failure_story text,
  invite_quota integer DEFAULT 5,
  used_invites integer DEFAULT 0,
  collaboration_count integer DEFAULT 0,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Users can read own data" ON users
  FOR SELECT TO authenticated
  USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON users
  FOR UPDATE TO authenticated
  USING (auth.uid() = id);

CREATE POLICY "Public can read basic user info" ON users
  FOR SELECT TO public
  USING (is_active = true);

CREATE POLICY "Users can insert own profile" ON users
  FOR INSERT TO authenticated
  WITH CHECK (auth.uid() = id);

-- İndeksler
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active) WHERE is_active = true;
CREATE INDEX idx_users_collaboration ON users(collaboration_count DESC);

-- Updated at trigger için function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Updated at trigger
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();