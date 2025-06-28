/*
  # Messages Tablosu Oluşturma

  1. Yeni Tablo
    - `messages`
      - `id` (uuid, primary key)
      - `conversation_id` (uuid, not null)
      - `sender_id` (uuid, not null)
      - `sender_name` (text, not null)
      - `content` (text, not null)
      - `is_read` (boolean, default false)
      - `created_at` (timestamptz, default now())
      - `is_active` (boolean, default true)

  2. Güvenlik
    - RLS etkinleştirildi
    - Sadece konuşmaya dahil olan kullanıcılar mesajları görebilir
    - Authenticated kullanıcılar mesaj gönderebilir

  3. İndeksler
    - Konuşma bazlı hızlı sorgular için
    - Zaman sıralaması için
    - Okunmamış mesajlar için
*/

-- Messages tablosu oluştur
CREATE TABLE IF NOT EXISTS messages (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id uuid NOT NULL,
  sender_id uuid NOT NULL,
  sender_name text NOT NULL,
  content text NOT NULL,
  is_read boolean DEFAULT false,
  created_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Users can read conversation messages" ON messages
  FOR SELECT TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM conversations 
      WHERE conversations.id = messages.conversation_id 
      AND (conversations.user1_id = auth.uid() OR conversations.user2_id = auth.uid())
      AND conversations.is_active = true
    )
  );

CREATE POLICY "Users can send messages" ON messages
  FOR INSERT TO authenticated
  WITH CHECK (
    auth.uid() = sender_id AND
    EXISTS (
      SELECT 1 FROM conversations 
      WHERE conversations.id = messages.conversation_id 
      AND (conversations.user1_id = auth.uid() OR conversations.user2_id = auth.uid())
      AND conversations.is_active = true
    )
  );

CREATE POLICY "Users can update own messages" ON messages
  FOR UPDATE TO authenticated
  USING (auth.uid() = sender_id);

-- İndeksler
CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_active ON messages(is_active) WHERE is_active = true;
CREATE INDEX idx_messages_created ON messages(created_at DESC);
CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at DESC);
CREATE INDEX idx_messages_unread ON messages(is_read) WHERE is_read = false;