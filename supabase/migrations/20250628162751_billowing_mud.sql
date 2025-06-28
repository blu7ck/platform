/*
  # Create flows table

  1. New Tables
    - `flows`
      - `id` (uuid, primary key)
      - `user_id` (uuid, foreign key to users)
      - `author_name` (text)
      - `title` (text)
      - `content` (text)
      - `allow_collaboration` (boolean)
      - `allow_sharing` (boolean)
      - `response_count` (integer)
      - `created_at` (timestamp)
      - `updated_at` (timestamp)
      - `is_active` (boolean)
  2. Security
    - Enable RLS on `flows` table
    - Add policies for authenticated users to read/create/update flows
    - Add policy for public to read active flows
  3. Indexes
    - Index on user_id for user's flows
    - Index on active flows
    - Index on creation date for sorting
*/

-- Flows tablosu
CREATE TABLE IF NOT EXISTS flows (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  author_name text NOT NULL,
  title text NOT NULL,
  content text NOT NULL,
  allow_collaboration boolean DEFAULT false,
  allow_sharing boolean DEFAULT false,
  response_count integer DEFAULT 0,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE flows ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Users can read own flows" ON flows
  FOR SELECT TO authenticated
  USING (auth.uid() = user_id);

CREATE POLICY "Public can read active flows" ON flows
  FOR SELECT TO public
  USING (is_active = true);

CREATE POLICY "Users can create flows" ON flows
  FOR INSERT TO authenticated
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own flows" ON flows
  FOR UPDATE TO authenticated
  USING (auth.uid() = user_id);

-- İndeksler
CREATE INDEX idx_flows_user ON flows(user_id);
CREATE INDEX idx_flows_active ON flows(is_active) WHERE is_active = true;
CREATE INDEX idx_flows_created ON flows(created_at DESC);
CREATE INDEX idx_flows_collaboration ON flows(allow_collaboration) WHERE allow_collaboration = true;

-- Updated at trigger
CREATE TRIGGER update_flows_updated_at 
    BEFORE UPDATE ON flows 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();