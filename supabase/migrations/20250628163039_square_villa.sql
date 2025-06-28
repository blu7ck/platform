/*
  # Collaborations Tablosu Oluşturma

  1. Yeni Tablo
    - `collaborations`
      - `id` (uuid, primary key)
      - `initiator_id` (uuid, not null) - İşbirliğini başlatan
      - `participant_id` (uuid, not null) - İşbirliğine katılan
      - `initiator_name` (text, not null)
      - `participant_name` (text, not null)
      - `source_type` (text) - "flow", "spark", "profile"
      - `source_id` (uuid) - Kaynak içeriğin ID'si
      - `status` (text) - "pending", "approved", "active", "completed", "cancelled"
      - `title` (text, not null)
      - `description` (text)
      - `is_premium` (boolean, default false)
      - `created_at` (timestamptz, default now())
      - `updated_at` (timestamptz, default now())
      - `is_active` (boolean, default true)

  2. Güvenlik
    - RLS etkinleştirildi
    - Kullanıcılar kendi işbirliklerini okuyabilir/güncelleyebilir
    - Public aktif işbirlikleri okuyabilir
    - Authenticated kullanıcılar işbirliği oluşturabilir

  3. İndeksler
    - Kullanıcı bazlı sorgular için
    - Durum filtreleme için
    - Kaynak tip/ID bazlı sorgular için
*/

-- Collaborations tablosu oluştur
CREATE TABLE IF NOT EXISTS collaborations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  initiator_id uuid NOT NULL,
  participant_id uuid NOT NULL,
  initiator_name text NOT NULL,
  participant_name text NOT NULL,
  source_type text CHECK (source_type IN ('flow', 'spark', 'profile')),
  source_id uuid,
  status text DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'active', 'completed', 'cancelled')),
  title text NOT NULL,
  description text,
  is_premium boolean DEFAULT false,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE collaborations ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Users can read own collaborations" ON collaborations
  FOR SELECT TO authenticated
  USING (auth.uid() = initiator_id OR auth.uid() = participant_id);

CREATE POLICY "Public can read active collaborations" ON collaborations
  FOR SELECT TO public
  USING (is_active = true);

CREATE POLICY "Users can create collaborations" ON collaborations
  FOR INSERT TO authenticated
  WITH CHECK (auth.uid() = initiator_id);

CREATE POLICY "Users can update own collaborations" ON collaborations
  FOR UPDATE TO authenticated
  USING (auth.uid() = initiator_id OR auth.uid() = participant_id);

-- İndeksler
CREATE INDEX idx_collaborations_initiator ON collaborations(initiator_id);
CREATE INDEX idx_collaborations_participant ON collaborations(participant_id);
CREATE INDEX idx_collaborations_status ON collaborations(status);
CREATE INDEX idx_collaborations_active ON collaborations(is_active) WHERE is_active = true;
CREATE INDEX idx_collaborations_created ON collaborations(created_at DESC);
CREATE INDEX idx_collaborations_source ON collaborations(source_type, source_id);
CREATE INDEX idx_collaborations_pending ON collaborations(status) WHERE status = 'pending';

-- Updated at trigger
CREATE TRIGGER update_collaborations_updated_at
  BEFORE UPDATE ON collaborations
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();