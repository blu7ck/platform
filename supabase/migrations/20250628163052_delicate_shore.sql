/*
  # Conversations Tablosu Oluşturma

  1. Yeni Tablo
    - `conversations`
      - `id` (uuid, primary key)
      - `user1_id` (uuid, not null) - İlk kullanıcı
      - `user2_id` (uuid, not null) - İkinci kullanıcı
      - `user1_name` (text, not null)
      - `user2_name` (text, not null)
      - `conversation_type` (text) - "general", "collaboration"
      - `collaboration_id` (uuid) - Eğer işbirliği konuşması ise
      - `last_message` (text)
      - `last_message_at` (timestamptz)
      - `created_at` (timestamptz, default now())
      - `is_active` (boolean, default true)

  2. Güvenlik
    - RLS etkinleştirildi
    - Sadece konuşmaya dahil olan kullanıcılar erişebilir
    - Authenticated kullanıcılar konuşma başlatabilir

  3. İndeksler
    - Kullanıcı bazlı hızlı sorgular için
    - Son mesaj zamanına göre sıralama için
    - İşbirliği konuşmaları için
*/

-- Conversations tablosu oluştur
CREATE TABLE IF NOT EXISTS conversations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user1_id uuid NOT NULL,
  user2_id uuid NOT NULL,
  user1_name text NOT NULL,
  user2_name text NOT NULL,
  conversation_type text DEFAULT 'general' CHECK (conversation_type IN ('general', 'collaboration')),
  collaboration_id uuid,
  last_message text,
  last_message_at timestamptz DEFAULT now(),
  created_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Users can read own conversations" ON conversations
  FOR SELECT TO authenticated
  USING (auth.uid() = user1_id OR auth.uid() = user2_id);

CREATE POLICY "Users can create conversations" ON conversations
  FOR INSERT TO authenticated
  WITH CHECK (auth.uid() = user1_id OR auth.uid() = user2_id);

CREATE POLICY "Users can update own conversations" ON conversations
  FOR UPDATE TO authenticated
  USING (auth.uid() = user1_id OR auth.uid() = user2_id);

-- İndeksler
CREATE INDEX idx_conversations_user1 ON conversations(user1_id);
CREATE INDEX idx_conversations_user2 ON conversations(user2_id);
CREATE INDEX idx_conversations_active ON conversations(is_active) WHERE is_active = true;
CREATE INDEX idx_conversations_last_message ON conversations(last_message_at DESC);
CREATE INDEX idx_conversations_collaboration ON conversations(collaboration_id) WHERE collaboration_id IS NOT NULL;

-- Unique constraint: İki kullanıcı arasında sadece bir genel konuşma olabilir
CREATE UNIQUE INDEX idx_conversations_unique_users 
  ON conversations(LEAST(user1_id, user2_id), GREATEST(user1_id, user2_id)) 
  WHERE conversation_type = 'general' AND is_active = true;