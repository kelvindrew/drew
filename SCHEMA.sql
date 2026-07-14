-- Schéma de base de données PostgreSQL pour l'Application Premium Companion
-- Orienté Supabase avec Row Level Security (RLS)

-- Extension pour la génération d'UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- Extension pour la géolocalisation
CREATE EXTENSION IF NOT EXISTS postgis;

----------------------------------------------------
-- 1. UTILISATEURS (USERS)
----------------------------------------------------
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    auth_id UUID UNIQUE NOT NULL, -- Lié à l'auth Supabase
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('client', 'companion', 'admin')),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    date_of_birth DATE,
    profile_picture_url TEXT,
    bio TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    is_premium BOOLEAN DEFAULT FALSE,
    kyc_status VARCHAR(50) DEFAULT 'pending', -- pending, verified, rejected
    location GEOGRAPHY(Point, 4326),
    city VARCHAR(100),
    country VARCHAR(100),
    languages TEXT[],
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

----------------------------------------------------
-- 2. PROFILS COMPANIONS
----------------------------------------------------
CREATE TABLE companion_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    presentation_video_url TEXT,
    interests TEXT[],
    hourly_rate DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    daily_rate DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    max_distance_km INTEGER DEFAULT 50,
    experience_years INTEGER DEFAULT 0,
    total_bookings INTEGER DEFAULT 0,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    is_top_companion BOOLEAN DEFAULT FALSE,
    xp_points INTEGER DEFAULT 0,
    level INTEGER DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

----------------------------------------------------
-- 3. ACTIVITÉS DISPONIBLES
----------------------------------------------------
CREATE TABLE activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

-- Table de liaison Companion <-> Activités
CREATE TABLE companion_activities (
    companion_id UUID REFERENCES companion_profiles(id) ON DELETE CASCADE,
    activity_id UUID REFERENCES activities(id) ON DELETE CASCADE,
    PRIMARY KEY (companion_id, activity_id)
);

----------------------------------------------------
-- 4. RÉSERVATIONS (BOOKINGS)
----------------------------------------------------
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID NOT NULL REFERENCES users(id),
    companion_id UUID NOT NULL REFERENCES companion_profiles(id),
    activity_id UUID NOT NULL REFERENCES activities(id),
    status VARCHAR(50) NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'accepted', 'rejected', 'cancelled', 'in_progress', 'completed', 'disputed')),
    scheduled_date TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_hours INTEGER NOT NULL,
    location_address TEXT NOT NULL,
    location_coords GEOGRAPHY(Point, 4326),
    total_price DECIMAL(10, 2) NOT NULL,
    platform_fee DECIMAL(10, 2) NOT NULL,
    client_instructions TEXT,
    cancellation_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

----------------------------------------------------
-- 5. PAIEMENTS & TRANSACTIONS
----------------------------------------------------
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID REFERENCES bookings(id) ON DELETE SET NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    stripe_payment_intent_id VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'EUR',
    status VARCHAR(50) NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'held_in_escrow', 'released', 'refunded', 'failed')),
    payment_method VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

----------------------------------------------------
-- 6. AVIS (REVIEWS)
----------------------------------------------------
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID NOT NULL UNIQUE REFERENCES bookings(id),
    reviewer_id UUID NOT NULL REFERENCES users(id),
    reviewee_id UUID NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_flagged_by_ai BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

----------------------------------------------------
-- 7. MESSAGERIE (CHATS)
----------------------------------------------------
CREATE TABLE chat_rooms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID REFERENCES bookings(id) ON DELETE SET NULL, -- Peut être null si chat pré-réservation autorisé
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE chat_participants (
    room_id UUID REFERENCES chat_rooms(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (room_id, user_id)
);

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id UUID NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id),
    content TEXT,
    media_url TEXT,
    message_type VARCHAR(20) DEFAULT 'text' CHECK (message_type IN ('text', 'image', 'video', 'audio', 'system')),
    is_read BOOLEAN DEFAULT FALSE,
    is_translated_by_ai BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

----------------------------------------------------
-- 8. SÉCURITÉ & SIGNALEMENTS (REPORTS)
----------------------------------------------------
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID NOT NULL REFERENCES users(id),
    reported_id UUID NOT NULL REFERENCES users(id),
    booking_id UUID REFERENCES bookings(id),
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'open' CHECK (status IN ('open', 'investigating', 'resolved')),
    ai_risk_score INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

----------------------------------------------------
-- INDEXES POUR LES PERFORMANCES
----------------------------------------------------
CREATE INDEX idx_users_auth_id ON users(auth_id);
CREATE INDEX idx_users_location ON users USING GIST(location);
CREATE INDEX idx_bookings_client ON bookings(client_id);
CREATE INDEX idx_bookings_companion ON bookings(companion_id);
CREATE INDEX idx_messages_room ON messages(room_id, created_at DESC);

----------------------------------------------------
-- ROW LEVEL SECURITY (RLS) POLICIES
----------------------------------------------------
-- Activer RLS sur les tables sensibles
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

-- SECURITY FIX: Restrict access to PII. Users can ONLY see their own data in the 'users' table.
-- Public profile data should be accessed through specific views or the 'companion_profiles' table.
CREATE POLICY "Users can only view their own private data" ON users
    FOR SELECT USING (auth.uid() = auth_id);

-- Un utilisateur ne peut modifier que son propre profil
CREATE POLICY "Users can update their own profile" ON users
    FOR UPDATE USING (auth.uid() = auth_id);

-- Réservations : un client ou un companion ne voit que ses propres réservations
CREATE POLICY "Users can view their own bookings" ON bookings
    FOR SELECT USING (
        auth.uid() IN (
            SELECT auth_id FROM users WHERE id = client_id OR id IN (
                SELECT user_id FROM companion_profiles WHERE id = bookings.companion_id
            )
        )
    );

-- Messages : les utilisateurs ne voient que les messages des salles où ils sont participants
CREATE POLICY "Users can view messages in their rooms" ON messages
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM chat_participants cp
            JOIN users u ON u.id = cp.user_id
            WHERE cp.room_id = messages.room_id AND u.auth_id = auth.uid()
        )
    );

-- Activer RLS sur TOUTES les tables
ALTER TABLE companion_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE activities ENABLE ROW LEVEL SECURITY;
ALTER TABLE companion_activities ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE reports ENABLE ROW LEVEL SECURITY;

-- POLITIQUES DE BASE (Lecture publique pour les catalogues, restreint pour le reste)

-- Companion Profiles : Tout le monde peut voir les profils vérifiés, seul le propriétaire peut modifier
CREATE POLICY "Public profiles are viewable by everyone" ON companion_profiles FOR SELECT USING (true);
CREATE POLICY "Users can insert their own profile" ON companion_profiles FOR INSERT WITH CHECK (user_id IN (SELECT id FROM users WHERE auth_id = auth.uid()));
CREATE POLICY "Users can update their own profile" ON companion_profiles FOR UPDATE USING (user_id IN (SELECT id FROM users WHERE auth_id = auth.uid()));

-- Activities : Tout le monde peut lire le catalogue
CREATE POLICY "Activities are viewable by everyone" ON activities FOR SELECT USING (true);

-- Transactions : Seul le backend (Service Role) et l'utilisateur concerné peuvent voir.
-- L'insertion/mise à jour est réservée au backend (pas de policy INSERT/UPDATE publique pour éviter les fraudes)
CREATE POLICY "Users view their own transactions" ON transactions FOR SELECT USING (user_id IN (SELECT id FROM users WHERE auth_id = auth.uid()));

-- Reports : Un utilisateur peut insérer un signalement
CREATE POLICY "Users can insert reports" ON reports FOR INSERT WITH CHECK (reporter_id IN (SELECT id FROM users WHERE auth_id = auth.uid()));

-- Views (Vue sécurisée pour les infos publiques des utilisateurs)
CREATE OR REPLACE VIEW public_user_profiles AS
SELECT id, role, first_name, profile_picture_url, city, is_verified, is_premium, languages
FROM users;

-- Accès en lecture à la vue (Géré par Supabase pour les vues)
GRANT SELECT ON public_user_profiles TO authenticated, anon;

-- INSERT policies pour Users et Bookings
CREATE POLICY "Users can insert their own user record" ON users FOR INSERT WITH CHECK (auth_id = auth.uid());
CREATE POLICY "Clients can create bookings" ON bookings FOR INSERT WITH CHECK (client_id IN (SELECT id FROM users WHERE auth_id = auth.uid()));
