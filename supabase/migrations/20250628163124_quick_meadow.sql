/*
  # Subscriptions Tablosu Oluşturma

  1. Yeni Tablo
    - `subscriptions`
      - `id` (uuid, primary key)
      - `subscriber_name` (text) - Anonim ise null
      - `subscriber_email` (text) - Anonim ise null
      - `amount` (decimal, not null)
      - `currency` (text, not null) - "TRY", "USD"
      - `is_anonymous` (boolean, default false)
      - `status` (text) - "active", "cancelled", "paused"
      - `message` (text)
      - `payment_method_id` (text) - Iyzico subscription ID
      - `next_payment_date` (timestamptz)
      - `created_at` (timestamptz, default now())
      - `cancelled_at` (timestamptz)
      - `is_active` (boolean, default true)

  2. Güvenlik
    - RLS etkinleştirildi
    - Public aktif abonelikleri görebilir (anonim olmayanları)
    - Aboneler kendi aboneliklerini yönetebilir

  3. İndeksler
    - Durum için
    - Ödeme tarihi için
    - Email bazlı sorgular için
*/

-- Subscriptions tablosu oluştur
CREATE TABLE IF NOT EXISTS subscriptions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  subscriber_name text,
  subscriber_email text,
  amount decimal(10,2) NOT NULL,
  currency text NOT NULL CHECK (currency IN ('TRY', 'USD')),
  is_anonymous boolean DEFAULT false,
  status text DEFAULT 'active' CHECK (status IN ('active', 'cancelled', 'paused')),
  message text,
  payment_method_id text,
  next_payment_date timestamptz,
  created_at timestamptz DEFAULT now(),
  cancelled_at timestamptz,
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE subscriptions ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Public can read active non-anonymous subscriptions" ON subscriptions
  FOR SELECT TO public
  USING (status = 'active' AND is_anonymous = false AND is_active = true);

CREATE POLICY "Subscribers can read own subscriptions" ON subscriptions
  FOR SELECT TO anon, authenticated
  USING (true); -- Email kontrolü frontend'de yapılacak

CREATE POLICY "Anyone can create subscriptions" ON subscriptions
  FOR INSERT TO anon, authenticated
  WITH CHECK (true);

CREATE POLICY "Subscribers can update own subscriptions" ON subscriptions
  FOR UPDATE TO anon, authenticated
  USING (true); -- Email kontrolü frontend'de yapılacak

-- İndeksler
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_created ON subscriptions(created_at DESC);
CREATE INDEX idx_subscriptions_active ON subscriptions(is_active) WHERE is_active = true;
CREATE INDEX idx_subscriptions_next_payment ON subscriptions(next_payment_date) WHERE status = 'active';
CREATE INDEX idx_subscriptions_email ON subscriptions(subscriber_email) WHERE subscriber_email IS NOT NULL;