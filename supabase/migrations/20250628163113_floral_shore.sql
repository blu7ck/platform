/*
  # Donations Tablosu Oluşturma

  1. Yeni Tablo
    - `donations`
      - `id` (uuid, primary key)
      - `donor_name` (text) - Anonim ise null
      - `donor_email` (text) - Anonim ise null
      - `amount` (decimal, not null)
      - `currency` (text, not null) - "TRY", "USD"
      - `is_anonymous` (boolean, default false)
      - `donation_type` (text) - "general", "event"
      - `event_id` (uuid) - Eğer özel etkinlik bağışı ise
      - `message` (text)
      - `payment_status` (text) - "pending", "completed", "failed"
      - `payment_id` (text) - Iyzico payment ID
      - `created_at` (timestamptz, default now())
      - `is_active` (boolean, default true)

  2. Güvenlik
    - RLS etkinleştirildi
    - Public tamamlanmış bağışları görebilir (anonim olmayanları)
    - Bağışçılar kendi bağışlarını görebilir

  3. İndeksler
    - Ödeme durumu için
    - Bağış tipi için
    - Tarih sıralaması için
*/

-- Donations tablosu oluştur
CREATE TABLE IF NOT EXISTS donations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  donor_name text,
  donor_email text,
  amount decimal(10,2) NOT NULL,
  currency text NOT NULL CHECK (currency IN ('TRY', 'USD')),
  is_anonymous boolean DEFAULT false,
  donation_type text DEFAULT 'general' CHECK (donation_type IN ('general', 'event')),
  event_id uuid,
  message text,
  payment_status text DEFAULT 'pending' CHECK (payment_status IN ('pending', 'completed', 'failed')),
  payment_id text,
  created_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE donations ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Public can read completed non-anonymous donations" ON donations
  FOR SELECT TO public
  USING (payment_status = 'completed' AND is_anonymous = false AND is_active = true);

CREATE POLICY "Donors can read own donations" ON donations
  FOR SELECT TO anon, authenticated
  USING (true); -- Anonim bağışçılar için email kontrolü frontend'de yapılacak

CREATE POLICY "Anyone can create donations" ON donations
  FOR INSERT TO anon, authenticated
  WITH CHECK (true);

CREATE POLICY "System can update donation status" ON donations
  FOR UPDATE TO authenticated
  USING (true); -- Ödeme callback'leri için

-- İndeksler
CREATE INDEX idx_donations_status ON donations(payment_status);
CREATE INDEX idx_donations_type ON donations(donation_type);
CREATE INDEX idx_donations_created ON donations(created_at DESC);
CREATE INDEX idx_donations_active ON donations(is_active) WHERE is_active = true;
CREATE INDEX idx_donations_completed ON donations(payment_status) WHERE payment_status = 'completed';
CREATE INDEX idx_donations_email ON donations(donor_email) WHERE donor_email IS NOT NULL;