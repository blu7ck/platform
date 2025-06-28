/*
  # Spark Interactions Tablosu Oluşturma

  1. Yeni Tablo
    - `spark_interactions`
      - `id` (uuid, primary key)
      - `spark_id` (uuid, not null)
      - `user_id` (uuid, not null)
      - `author_name` (text, not null)
      - `interaction_type` (text, not null) - Etkileşim kartı tipi
      - `created_at` (timestamptz, default now())
      - `is_active` (boolean, default true)

  2. Güvenlik
    - RLS etkinleştirildi
    - Public aktif etkileşimleri okuyabilir
    - Authenticated kullanıcılar etkileşim oluşturabilir
    - Kullanıcılar kendi etkileşimlerini okuyabilir/güncelleyebilir

  3. İndeksler
    - Spark bazlı sorgular için
    - Kullanıcı bazlı sorgular için
    - Aktif etkileşimler için
    - Kronolojik sıralama için

  4. Kısıtlamalar
    - Kullanıcı aynı spark'a aynı tipte sadece bir etkileşim yapabilir
*/

-- Spark Interactions tablosu oluştur
CREATE TABLE IF NOT EXISTS spark_interactions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  spark_id uuid NOT NULL,
  user_id uuid NOT NULL,
  author_name text NOT NULL,
  interaction_type text NOT NULL,
  created_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE spark_interactions ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Public can read active interactions" ON spark_interactions
  FOR SELECT TO public
  USING (is_active = true);

CREATE POLICY "Users can create interactions" ON spark_interactions
  FOR INSERT TO authenticated
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can read own interactions" ON spark_interactions
  FOR SELECT TO authenticated
  USING (auth.uid() = user_id);

CREATE POLICY "Users can update own interactions" ON spark_interactions
  FOR UPDATE TO authenticated
  USING (auth.uid() = user_id);

-- İndeksler
CREATE INDEX idx_spark_interactions_spark ON spark_interactions(spark_id);
CREATE INDEX idx_spark_interactions_user ON spark_interactions(user_id);
CREATE INDEX idx_spark_interactions_active ON spark_interactions(is_active) WHERE is_active = true;
CREATE INDEX idx_spark_interactions_created ON spark_interactions(created_at DESC);
CREATE INDEX idx_spark_interactions_spark_active ON spark_interactions(spark_id, is_active) WHERE is_active = true;
CREATE INDEX idx_spark_interactions_type ON spark_interactions(interaction_type);

-- Unique constraint: Kullanıcı aynı spark'a aynı tipte sadece bir etkileşim yapabilir
CREATE UNIQUE INDEX idx_spark_interactions_unique_user_type 
  ON spark_interactions(spark_id, user_id, interaction_type) 
  WHERE is_active = true;