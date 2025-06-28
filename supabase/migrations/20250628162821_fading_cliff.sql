/*
  # Flow Responses Table

  1. New Tables
    - `flow_responses`
      - `id` (uuid, primary key)
      - `flow_id` (uuid, foreign key to flows)
      - `user_id` (uuid, references auth.users)
      - `author_name` (text)
      - `content` (text)
      - `created_at` (timestamp)
      - `is_active` (boolean)

  2. Security
    - Enable RLS on `flow_responses` table
    - Add policies for authenticated users to read/create responses
    - Add policy for public to read active responses

  3. Indexes
    - Index on flow_id for efficient querying
    - Index on user_id for user's responses
    - Index on created_at for chronological ordering
    - Index on active responses
*/

-- Flow Responses tablosu
CREATE TABLE IF NOT EXISTS flow_responses (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  flow_id uuid NOT NULL,
  user_id uuid NOT NULL,
  author_name text NOT NULL,
  content text NOT NULL,
  created_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true
);

-- RLS etkinleştir
ALTER TABLE flow_responses ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Users can read own responses" ON flow_responses
  FOR SELECT TO authenticated
  USING (auth.uid() = user_id);

CREATE POLICY "Public can read active responses" ON flow_responses
  FOR SELECT TO public
  USING (is_active = true);

CREATE POLICY "Users can create responses" ON flow_responses
  FOR INSERT TO authenticated
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own responses" ON flow_responses
  FOR UPDATE TO authenticated
  USING (auth.uid() = user_id);

-- İndeksler
CREATE INDEX idx_flow_responses_flow ON flow_responses(flow_id);
CREATE INDEX idx_flow_responses_user ON flow_responses(user_id);
CREATE INDEX idx_flow_responses_active ON flow_responses(is_active) WHERE is_active = true;
CREATE INDEX idx_flow_responses_created ON flow_responses(created_at ASC);
CREATE INDEX idx_flow_responses_flow_active ON flow_responses(flow_id, is_active) WHERE is_active = true;