/*
  # Create users table

  1. New Tables
    - `users`
      - `id` (uuid, primary key, references auth.users)
      - `email` (text, unique, not null)
      - `full_name` (text, not null)
      - `profession` (text, not null, with check constraint)
      - `tckn` (text, unique, with validation)
      - `title` (text, nullable)
      - `website` (text, nullable)
      - `success_story` (text, nullable)
      - `failure_story` (text, nullable)
      - `invite_quota` (integer, default 5)
      - `used_invites` (integer, default 0)
      - `collaboration_count` (integer, default 0)
      - `created_at` (timestamptz, default now())
      - `updated_at` (timestamptz, default now())
      - `is_active` (boolean, default true)

  2. Security
    - Enable RLS on `users` table
    - Add policy for public to view all profiles
    - Add policy for authenticated users to view own profile
    - Add policy for authenticated users to update own profile

  3. Triggers
    - Auto-update `updated_at` column on row updates

  4. Indexes
    - Index on email for fast lookups
    - Index on profession for filtering
    - Partial index on active users
*/

-- Kullanıcılar tablosu
CREATE TABLE IF NOT EXISTS users (
  id uuid PRIMARY KEY REFERENCES auth.users(id),
  email text UNIQUE NOT NULL,
  full_name text NOT NULL,
  profession text NOT NULL CHECK (profession IN ('Avukat', 'Yazar', 'Gazeteci', 'Influencer', 'Yazılımcı', 'CEO', 'CTO', 'Diğer')),
  tckn text UNIQUE CHECK (tckn ~ '^[0-9]{11}$'),
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
CREATE POLICY "Public profiles viewable" ON users
  FOR SELECT TO public
  USING (true);

CREATE POLICY "Users can view own profile" ON users
  FOR SELECT TO authenticated
  USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON users
  FOR UPDATE TO authenticated
  USING (auth.uid() = id);

-- Updated_at trigger için fonksiyon
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ language 'plpgsql';

-- Updated_at trigger
CREATE TRIGGER update_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- İndeksler
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_profession ON users(profession);
CREATE INDEX idx_users_active ON users(is_active) WHERE is_active = true;