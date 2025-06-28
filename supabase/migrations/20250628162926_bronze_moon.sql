/*
  # Sparks Tablosu Oluşturma

  1. Yeni Tablo
    - `sparks`
      - `id` (uuid, primary key)
      - `user_id` (uuid, not null)
      - `author_name` (text, not null)
      - `content` (text, not null)
      - `allow_collaboration` (boolean, default false)
      - `interaction_count` (integer, default 0)
      - `created_at` (timestamptz, default now())
      - `updated_at` (timestamptz, default now())
      - `is_active` (boolean, default true)

  2. Güvenlik
    - RLS etkinleştirildi
    - Kullanıcılar kendi spark'larını okuyabilir/güncelleyebilir
    - Public aktif spark'ları okuyabilir
    - Authenticated kullanıcılar spark oluşturabilir

  3. İndeksler
    - Kullanıcı bazlı sorgular için
    - Aktif spark'lar için
    - Kronolojik sıralama için
    - İşbirliği filtreleme için
*/

-- Sparks tablosu oluştur
CREATE TABLE IF NOT EXISTS sparks (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  author_name text NOT NULL,
  content text NOT NULL,
  allow_collaboration boolean DEFAULT false,
  interaction_count integer DEFAULT 0,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE sparks ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Public can read active sparks" ON sparks
  FOR SELECT TO public
  USING (is_active = true);

CREATE POLICY "Users can create sparks" ON sparks
  FOR INSERT TO authenticated
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can read own sparks" ON sparks
  FOR SELECT TO authenticated
  USING (auth.uid() = user_id);

CREATE POLICY "Users can update own sparks" ON sparks
  FOR UPDATE TO authenticated
  USING (auth.uid() = user_id);

-- İndeksler
CREATE INDEX idx_sparks_user ON sparks(user_id);
CREATE INDEX idx_sparks_active ON sparks(is_active) WHERE is_active = true;
CREATE INDEX idx_sparks_created ON sparks(created_at DESC);
CREATE INDEX idx_sparks_collaboration ON sparks(allow_collaboration) WHERE allow_collaboration = true;

-- Updated at trigger
CREATE TRIGGER update_sparks_updated_at
  BEFORE UPDATE ON sparks
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();