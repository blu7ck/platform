/*
  # Sparks Tablosu Oluşturma

  1. Yeni Tablo
    - `sparks`
      - `id` (uuid, primary key)
      - `user_id` (uuid, not null)
      - `author_name` (text, not null)
      - `content` (text, not null)
      - `allow_collaboration` (boolean, default false)
      - `interaction_count` (integer, default 0)
      - `created_at` (timestamptz, default now())
      - `updated_at` (timestamptz, default now())
      - `is_active` (boolean, default true)

  2. Güvenlik
    - RLS etkinleştirildi
    - Kullanıcılar kendi spark'larını okuyabilir/güncelleyebilir
    - Public aktif spark'ları okuyabilir
    - Authenticated kullanıcılar spark oluşturabilir

  3. İndeksler
    - Kullanıcı bazlı sorgular için
    - Aktif spark'lar için
    - Kronolojik sıralama için
    - İşbirliği filtreleme için
</parameter>