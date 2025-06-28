/*
  # Database Schema Improvements - Corrected Version

  1. Data Validation Constraints
    - Email format validation
    - Content length limits
    - Positive amount checks
    - Date logic validation

  2. Performance Optimizations
    - Composite indexes for common queries
    - Partial indexes for active records

  3. Utility Functions
    - User statistics
    - Content engagement metrics
    - Permission validation

  4. New Features
    - Notification preferences
    - User activity tracking
    - Content moderation system
    - Analytics materialized view
*/

-- 1. ADD MISSING CHECK CONSTRAINTS TO EXISTING TABLES
ALTER TABLE users ADD CONSTRAINT users_email_format_check 
  CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

ALTER TABLE users ADD CONSTRAINT users_full_name_length_check 
  CHECK (length(trim(full_name)) >= 2);

ALTER TABLE flows ADD CONSTRAINT flows_title_length_check 
  CHECK (length(trim(title)) >= 3 AND length(title) <= 200);

ALTER TABLE flows ADD CONSTRAINT flows_content_length_check 
  CHECK (length(trim(content)) >= 10 AND length(content) <= 2000);

ALTER TABLE sparks ADD CONSTRAINT sparks_content_length_check 
  CHECK (length(trim(content)) >= 5 AND length(content) <= 280);

ALTER TABLE flow_responses ADD CONSTRAINT flow_responses_content_length_check 
  CHECK (length(trim(content)) >= 1 AND length(content) <= 500);

ALTER TABLE messages ADD CONSTRAINT messages_content_length_check 
  CHECK (length(trim(content)) >= 1 AND length(content) <= 1000);

ALTER TABLE collaborations ADD CONSTRAINT collaborations_title_length_check 
  CHECK (length(trim(title)) >= 3 AND length(title) <= 200);

ALTER TABLE invite_codes ADD CONSTRAINT invite_codes_expiry_future_check 
  CHECK (expires_at > created_at);

ALTER TABLE donations ADD CONSTRAINT donations_amount_positive_check 
  CHECK (amount > 0);

ALTER TABLE subscriptions ADD CONSTRAINT subscriptions_amount_positive_check 
  CHECK (amount > 0);

-- 2. ADD COMPOSITE INDEXES FOR PERFORMANCE
CREATE INDEX IF NOT EXISTS idx_flows_user_active_created 
  ON flows (user_id, is_active, created_at DESC) 
  WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_sparks_user_active_created 
  ON sparks (user_id, is_active, created_at DESC) 
  WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_messages_conversation_created 
  ON messages (conversation_id, created_at DESC) 
  WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_collaborations_user_status 
  ON collaborations (initiator_id, status) 
  WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_collaborations_participant_status 
  ON collaborations (participant_id, status) 
  WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_invite_codes_email_active 
  ON invite_codes (invited_email, is_active, expires_at) 
  WHERE is_active = true AND NOT is_used;

-- 3. ADD UTILITY FUNCTIONS
CREATE OR REPLACE FUNCTION get_user_stats(user_uuid UUID)
RETURNS JSON AS $$
DECLARE
  result JSON;
BEGIN
  SELECT json_build_object(
    'flows_count', (SELECT COUNT(*) FROM flows WHERE user_id = user_uuid AND is_active = true),
    'sparks_count', (SELECT COUNT(*) FROM sparks WHERE user_id = user_uuid AND is_active = true),
    'collaborations_count', (SELECT collaboration_count FROM users WHERE id = user_uuid),
    'responses_given', (SELECT COUNT(*) FROM flow_responses WHERE user_id = user_uuid AND is_active = true),
    'interactions_made', (SELECT COUNT(*) FROM spark_interactions WHERE user_id = user_uuid AND is_active = true)
  ) INTO result;
  
  RETURN result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION get_content_engagement(content_type TEXT, content_id UUID)
RETURNS JSON AS $$
DECLARE
  result JSON;
BEGIN
  IF content_type = 'flow' THEN
    SELECT json_build_object(
      'responses_count', (SELECT COUNT(*) FROM flow_responses WHERE flow_id = content_id AND is_active = true),
      'collaboration_requests', (SELECT COUNT(*) FROM collaborations WHERE source_type = 'flow' AND source_id = content_id AND is_active = true)
    ) INTO result;
  ELSIF content_type = 'spark' THEN
    SELECT json_build_object(
      'interactions_count', (SELECT COUNT(*) FROM spark_interactions WHERE spark_id = content_id AND is_active = true),
      'collaboration_requests', (SELECT COUNT(*) FROM collaborations WHERE source_type = 'spark' AND source_id = content_id AND is_active = true)
    ) INTO result;
  END IF;
  
  RETURN result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 4. ADD CONTENT VALIDATION FUNCTION
CREATE OR REPLACE FUNCTION validate_content_permissions(
  user_uuid UUID,
  content_type TEXT,
  content_id UUID,
  action_type TEXT
)
RETURNS BOOLEAN AS $$
DECLARE
  is_owner BOOLEAN := false;
  allows_collaboration BOOLEAN := false;
BEGIN
  -- Check if user owns the content
  IF content_type = 'flow' THEN
    SELECT user_id = user_uuid, allow_collaboration 
    INTO is_owner, allows_collaboration
    FROM flows 
    WHERE id = content_id AND is_active = true;
  ELSIF content_type = 'spark' THEN
    SELECT user_id = user_uuid, allow_collaboration 
    INTO is_owner, allows_collaboration
    FROM sparks 
    WHERE id = content_id AND is_active = true;
  END IF;
  
  -- Owner can do everything
  IF is_owner THEN
    RETURN true;
  END IF;
  
  -- For collaboration requests, check if collaboration is allowed
  IF action_type = 'collaborate' AND allows_collaboration THEN
    RETURN true;
  END IF;
  
  -- For responses/interactions, always allowed if content exists
  IF action_type IN ('respond', 'interact') THEN
    RETURN true;
  END IF;
  
  RETURN false;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 5. ADD NOTIFICATION PREFERENCES TABLE
CREATE TABLE IF NOT EXISTS notification_preferences (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  email_notifications BOOLEAN DEFAULT true,
  collaboration_requests BOOLEAN DEFAULT true,
  new_responses BOOLEAN DEFAULT true,
  new_interactions BOOLEAN DEFAULT true,
  new_messages BOOLEAN DEFAULT true,
  weekly_digest BOOLEAN DEFAULT true,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  is_active BOOLEAN DEFAULT true
);

-- Add RLS for notification preferences
ALTER TABLE notification_preferences ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own notification preferences"
  ON notification_preferences
  FOR ALL
  TO authenticated
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

-- Add index for notification preferences
CREATE INDEX IF NOT EXISTS idx_notification_preferences_user 
  ON notification_preferences (user_id) 
  WHERE is_active = true;

-- 6. ADD USER ACTIVITY TRACKING
CREATE TABLE IF NOT EXISTS user_activity (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  activity_type TEXT NOT NULL CHECK (activity_type IN (
    'login', 'logout', 'flow_created', 'spark_created', 
    'response_added', 'interaction_made', 'collaboration_requested',
    'message_sent', 'profile_updated'
  )),
  metadata JSONB,
  ip_address INET,
  user_agent TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- Add RLS for user activity
ALTER TABLE user_activity ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own activity"
  ON user_activity
  FOR SELECT
  TO authenticated
  USING (auth.uid() = user_id);

-- Add indexes for user activity
CREATE INDEX IF NOT EXISTS idx_user_activity_user_created 
  ON user_activity (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_user_activity_type_created 
  ON user_activity (activity_type, created_at DESC);

-- 7. ADD CONTENT MODERATION TABLE
CREATE TABLE IF NOT EXISTS content_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  reporter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content_type TEXT NOT NULL CHECK (content_type IN ('flow', 'spark', 'response', 'interaction', 'message')),
  content_id UUID NOT NULL,
  reason TEXT NOT NULL CHECK (reason IN (
    'spam', 'inappropriate', 'harassment', 'copyright', 'other'
  )),
  description TEXT,
  status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'reviewed', 'resolved', 'dismissed')),
  reviewed_by UUID REFERENCES users(id),
  reviewed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT now(),
  is_active BOOLEAN DEFAULT true
);

-- Add RLS for content reports
ALTER TABLE content_reports ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can create reports"
  ON content_reports
  FOR INSERT
  TO authenticated
  WITH CHECK (auth.uid() = reporter_id);

CREATE POLICY "Users can read own reports"
  ON content_reports
  FOR SELECT
  TO authenticated
  USING (auth.uid() = reporter_id);

-- Add indexes for content reports
CREATE INDEX IF NOT EXISTS idx_content_reports_status 
  ON content_reports (status, created_at DESC) 
  WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_content_reports_content 
  ON content_reports (content_type, content_id) 
  WHERE is_active = true;

-- 8. ADD ANALYTICS MATERIALIZED VIEW
CREATE MATERIALIZED VIEW IF NOT EXISTS daily_analytics AS
SELECT 
  date_trunc('day', created_at) as date,
  COUNT(*) FILTER (WHERE table_name = 'users') as new_users,
  COUNT(*) FILTER (WHERE table_name = 'flows') as new_flows,
  COUNT(*) FILTER (WHERE table_name = 'sparks') as new_sparks,
  COUNT(*) FILTER (WHERE table_name = 'flow_responses') as new_responses,
  COUNT(*) FILTER (WHERE table_name = 'spark_interactions') as new_interactions,
  COUNT(*) FILTER (WHERE table_name = 'collaborations') as new_collaborations,
  COUNT(*) FILTER (WHERE table_name = 'messages') as new_messages
FROM (
  SELECT created_at, 'users' as table_name FROM users WHERE is_active = true
  UNION ALL
  SELECT created_at, 'flows' as table_name FROM flows WHERE is_active = true
  UNION ALL
  SELECT created_at, 'sparks' as table_name FROM sparks WHERE is_active = true
  UNION ALL
  SELECT created_at, 'flow_responses' as table_name FROM flow_responses WHERE is_active = true
  UNION ALL
  SELECT created_at, 'spark_interactions' as table_name FROM spark_interactions WHERE is_active = true
  UNION ALL
  SELECT created_at, 'collaborations' as table_name FROM collaborations WHERE is_active = true
  UNION ALL
  SELECT created_at, 'messages' as table_name FROM messages WHERE is_active = true
) combined_data
GROUP BY date_trunc('day', created_at)
ORDER BY date DESC;

-- Create index for analytics view
CREATE UNIQUE INDEX IF NOT EXISTS idx_daily_analytics_date ON daily_analytics (date);

-- Add function to refresh analytics
CREATE OR REPLACE FUNCTION refresh_daily_analytics()
RETURNS void AS $$
BEGIN
  REFRESH MATERIALIZED VIEW CONCURRENTLY daily_analytics;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 9. ADD AUDIT TRIGGER FUNCTION (for future use)
CREATE OR REPLACE FUNCTION audit_trigger_function()
RETURNS TRIGGER AS $$
BEGIN
  -- Log important changes to a hypothetical audit table
  -- This is a placeholder for future audit functionality
  
  IF TG_OP = 'UPDATE' THEN
    NEW.updated_at = NOW();
    RETURN NEW;
  ELSIF TG_OP = 'INSERT' THEN
    NEW.created_at = COALESCE(NEW.created_at, NOW());
    NEW.updated_at = COALESCE(NEW.updated_at, NOW());
    RETURN NEW;
  END IF;
  
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;