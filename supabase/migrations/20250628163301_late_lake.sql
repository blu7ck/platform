/*
  # Foreign Key Constraints Ekleme

  1. Referential Integrity
    - Tüm tablolar arasında uygun foreign key bağlantıları
    - Cascade delete/update davranışları
    - Orphan record'ları önleme

  2. Eklenen Foreign Keys
    - invite_codes -> users (inviter_id)
    - flows -> users (user_id)
    - flow_responses -> flows (flow_id)
    - flow_responses -> users (user_id)
    - sparks -> users (user_id)
    - spark_interactions -> sparks (spark_id)
    - spark_interactions -> users (user_id)
    - collaborations -> users (initiator_id, participant_id)
    - collaborations -> flows/sparks (source_id)
    - conversations -> users (user1_id, user2_id)
    - conversations -> collaborations (collaboration_id)
    - messages -> conversations (conversation_id)
    - messages -> users (sender_id)

  3. Cascade Davranışları
    - User silindiğinde ilgili kayıtlar soft delete
    - Content silindiğinde responses/interactions cascade
*/

-- 1. INVITE_CODES Foreign Keys
ALTER TABLE invite_codes 
ADD CONSTRAINT fk_invite_codes_inviter 
FOREIGN KEY (inviter_id) REFERENCES users(id) ON DELETE CASCADE;

-- 2. FLOWS Foreign Keys
ALTER TABLE flows 
ADD CONSTRAINT fk_flows_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 3. FLOW_RESPONSES Foreign Keys
ALTER TABLE flow_responses 
ADD CONSTRAINT fk_flow_responses_flow 
FOREIGN KEY (flow_id) REFERENCES flows(id) ON DELETE CASCADE;

ALTER TABLE flow_responses 
ADD CONSTRAINT fk_flow_responses_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 4. SPARKS Foreign Keys
ALTER TABLE sparks 
ADD CONSTRAINT fk_sparks_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 5. SPARK_INTERACTIONS Foreign Keys
ALTER TABLE spark_interactions 
ADD CONSTRAINT fk_spark_interactions_spark 
FOREIGN KEY (spark_id) REFERENCES sparks(id) ON DELETE CASCADE;

ALTER TABLE spark_interactions 
ADD CONSTRAINT fk_spark_interactions_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 6. COLLABORATIONS Foreign Keys
ALTER TABLE collaborations 
ADD CONSTRAINT fk_collaborations_initiator 
FOREIGN KEY (initiator_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE collaborations 
ADD CONSTRAINT fk_collaborations_participant 
FOREIGN KEY (participant_id) REFERENCES users(id) ON DELETE CASCADE;

-- 7. CONVERSATIONS Foreign Keys
ALTER TABLE conversations 
ADD CONSTRAINT fk_conversations_user1 
FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE conversations 
ADD CONSTRAINT fk_conversations_user2 
FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE conversations 
ADD CONSTRAINT fk_conversations_collaboration 
FOREIGN KEY (collaboration_id) REFERENCES collaborations(id) ON DELETE SET NULL;

-- 8. MESSAGES Foreign Keys
ALTER TABLE messages 
ADD CONSTRAINT fk_messages_conversation 
FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE;

ALTER TABLE messages 
ADD CONSTRAINT fk_messages_sender 
FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE;

-- 9. Response Count Trigger Functions
-- Flow response count güncellemesi
CREATE OR REPLACE FUNCTION update_flow_response_count()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    UPDATE flows 
    SET response_count = response_count + 1 
    WHERE id = NEW.flow_id;
    RETURN NEW;
  ELSIF TG_OP = 'DELETE' THEN
    UPDATE flows 
    SET response_count = response_count - 1 
    WHERE id = OLD.flow_id;
    RETURN OLD;
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Spark interaction count güncellemesi
CREATE OR REPLACE FUNCTION update_spark_interaction_count()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    UPDATE sparks 
    SET interaction_count = interaction_count + 1 
    WHERE id = NEW.spark_id;
    RETURN NEW;
  ELSIF TG_OP = 'DELETE' THEN
    UPDATE sparks 
    SET interaction_count = interaction_count - 1 
    WHERE id = OLD.spark_id;
    RETURN OLD;
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 10. Triggers
CREATE TRIGGER trigger_update_flow_response_count
  AFTER INSERT OR DELETE ON flow_responses
  FOR EACH ROW
  EXECUTE FUNCTION update_flow_response_count();

CREATE TRIGGER trigger_update_spark_interaction_count
  AFTER INSERT OR DELETE ON spark_interactions
  FOR EACH ROW
  EXECUTE FUNCTION update_spark_interaction_count();

-- 11. Conversation last message update trigger
CREATE OR REPLACE FUNCTION update_conversation_last_message()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    UPDATE conversations 
    SET 
      last_message = NEW.content,
      last_message_at = NEW.created_at
    WHERE id = NEW.conversation_id;
    RETURN NEW;
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_conversation_last_message
  AFTER INSERT ON messages
  FOR EACH ROW
  EXECUTE FUNCTION update_conversation_last_message();

-- 12. User collaboration count update trigger
CREATE OR REPLACE FUNCTION update_user_collaboration_count()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'UPDATE' AND OLD.status != 'active' AND NEW.status = 'active' THEN
    -- İşbirliği aktif hale geldiğinde
    UPDATE users SET collaboration_count = collaboration_count + 1 WHERE id = NEW.initiator_id;
    UPDATE users SET collaboration_count = collaboration_count + 1 WHERE id = NEW.participant_id;
    RETURN NEW;
  ELSIF TG_OP = 'UPDATE' AND OLD.status = 'active' AND NEW.status != 'active' THEN
    -- İşbirliği aktif olmaktan çıktığında
    UPDATE users SET collaboration_count = collaboration_count - 1 WHERE id = NEW.initiator_id;
    UPDATE users SET collaboration_count = collaboration_count - 1 WHERE id = NEW.participant_id;
    RETURN NEW;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_user_collaboration_count
  AFTER UPDATE ON collaborations
  FOR EACH ROW
  EXECUTE FUNCTION update_user_collaboration_count();

-- 13. Invite usage tracking trigger
CREATE OR REPLACE FUNCTION update_invite_usage()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'UPDATE' AND OLD.is_used = false AND NEW.is_used = true THEN
    -- Davet kodu kullanıldığında inviter'ın used_invites sayısını artır
    UPDATE users 
    SET used_invites = used_invites + 1 
    WHERE id = NEW.inviter_id;
    RETURN NEW;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_invite_usage
  AFTER UPDATE ON invite_codes
  FOR EACH ROW
  EXECUTE FUNCTION update_invite_usage();