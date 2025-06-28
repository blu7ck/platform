/*
# Davet Kodları Tablosu

1. Yeni Tablolar
  - `invite_codes`
    - `id` (uuid, primary key)
    - `invite_code` (text, unique) - Benzersiz davet kodu
    - `inviter_id` (uuid) - Davet eden kullanıcı ID'si
    - `inviter_name` (text) - Davet eden kullanıcı adı
    - `inviter_email` (text) - Davet eden kullanıcı emaili
    - `invited_email` (text) - Davet edilen email
    - `is_used` (boolean) - Kullanılmış mı
    - `used_by_id` (uuid) - Kullanan kullanıcı ID'si
    - `used_by_email` (text) - Kullanan kullanıcı emaili
    - `used_at` (timestamptz) - Kullanım tarihi
    - `expires_at` (timestamptz) - Son kullanma tarihi
    - `invite_type` (text) - Davet tipi (admin/user/restricted)
    - `can_invite_others` (boolean) - Başkalarını davet edebilir mi
    - `created_at` (timestamptz)
    - `is_active` (boolean)

2. Güvenlik
  - RLS etkin
  - Kullanıcılar kendi davetlerini görebilir
  - Herkes geçerli kodları doğrulayabilir
  - Kimlik doğrulaması yapanlar kod oluşturabilir

3. İndeksler
  - Davet kodu için unique index
  - Davet eden kullanıcı için index
  - Aktif kodlar için composite index
  - Email için index
*/

-- Davet kodları tablosu
CREATE TABLE IF NOT EXISTS invite_codes (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  invite_code text UNIQUE NOT NULL,
  inviter_id uuid NOT NULL,
  inviter_name text NOT NULL,
  inviter_email text NOT NULL,
  invited_email text NOT NULL,
  is_used boolean DEFAULT false,
  used_by_id uuid,
  used_by_email text,
  used_at timestamptz,
  expires_at timestamptz NOT NULL,
  invite_type text DEFAULT 'user' CHECK (invite_type IN ('admin', 'user', 'restricted')),
  can_invite_others boolean DEFAULT true,
  created_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE invite_codes ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Users can view own invite codes" ON invite_codes
  FOR SELECT TO authenticated
  USING (auth.uid() = inviter_id);

CREATE POLICY "Public can validate invite codes" ON invite_codes
  FOR SELECT TO public
  USING (is_active = true AND NOT is_used AND expires_at > now());

CREATE POLICY "Users can create invite codes" ON invite_codes
  FOR INSERT TO authenticated
  WITH CHECK (auth.uid() = inviter_id);

CREATE POLICY "Users can update own invite codes" ON invite_codes
  FOR UPDATE TO authenticated
  USING (auth.uid() = inviter_id);

-- İndeksler
CREATE UNIQUE INDEX idx_invite_codes_code ON invite_codes(invite_code);
CREATE INDEX idx_invite_codes_inviter ON invite_codes(inviter_id);
CREATE INDEX idx_invite_codes_active ON invite_codes(is_active, is_used, expires_at) WHERE is_active = true;
CREATE INDEX idx_invite_codes_email ON invite_codes(invited_email);