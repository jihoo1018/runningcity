-- 필요 시 (PostGIS)
CREATE EXTENSION IF NOT EXISTS postgis;

-- =========================================
-- users 테이블 (PostgreSQL 16)
-- =========================================
CREATE TABLE IF NOT EXISTS users (
    user_id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    google_id             VARCHAR(255)  NOT NULL UNIQUE,
    email                 VARCHAR(100)  NOT NULL UNIQUE,
    nickname              VARCHAR(50),
    profile_image_url     VARCHAR(500),

    has_completed_onboarding BOOLEAN    NOT NULL DEFAULT FALSE,
    level                  INTEGER       NOT NULL DEFAULT 1,
    total_running_energy   BIGINT        NOT NULL DEFAULT 0,
    is_active              BOOLEAN       NOT NULL DEFAULT TRUE,

    created_at             timestamptz   NOT NULL DEFAULT now(),
    updated_at             timestamptz   NOT NULL DEFAULT now()
    );


CREATE TABLE IF NOT EXISTS run_session (
    session_id           BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    type                 TEXT         NOT NULL DEFAULT 'NORMAL' CHECK (type IN ('NORMAL','ENTRY')),
    base_id              BIGINT,
    device_type          TEXT         NOT NULL,                 -- ANDROID_PHONE | WEAR_OS
    start_at             timestamptz  NOT NULL,
    end_at               timestamptz,
    status               TEXT         NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE','CLOSING','FINALIZED')),
    closing_deadline     timestamptz,

    -- 요약값
    duration_sec         INTEGER,
    distance_km          NUMERIC(7,3),
    avg_pace_sec_per_km  INTEGER,
    calories_kcal        INTEGER,
    elevation_gain_m     INTEGER,
    avg_hr_bpm           SMALLINT,
    avg_cadence_spm      SMALLINT,

    -- JSONB 메타
    result_meta          JSONB,
    rewards_meta         JSONB,

    created_at           timestamptz  NOT NULL DEFAULT now(),
    updated_at           timestamptz  NOT NULL DEFAULT now()   -- DB 트리거로 자동 갱신
    );

CREATE INDEX IF NOT EXISTS run_session_user_time_idx
    ON run_session(user_id, start_at);
CREATE INDEX IF NOT EXISTS run_session_status_idx
    ON run_session(status);

-- ---- updated_at 자동 갱신 트리거 ----
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS trigger AS $$
BEGIN
    NEW.updated_at := now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_run_session_updated_at ON run_session;
CREATE TRIGGER trg_run_session_updated_at
    BEFORE UPDATE ON run_session
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =========================================
-- run_session_upload  (필요 컬럼만)
-- =========================================
CREATE TABLE IF NOT EXISTS run_session_upload (
    session_id      BIGINT PRIMARY KEY REFERENCES run_session(session_id) ON DELETE CASCADE,
    acked_until_seq INTEGER NOT NULL DEFAULT 0
    );

-- =========================================
-- run_point
-- =========================================
CREATE TABLE IF NOT EXISTS run_point (
    session_id   BIGINT NOT NULL REFERENCES run_session(session_id) ON DELETE CASCADE,
    seq          INTEGER NOT NULL CHECK (seq > 0),
    recorded_at  timestamptz NOT NULL,
    geom         geometry(PointZ, 5179) NOT NULL,   -- X=lon, Y=lat, Z=alt (DB는 5179)
    speed_mps    REAL,
    hr_bpm       SMALLINT,
    cadence_spm  SMALLINT,
    source       TEXT,                              -- "PHONE" | "WATCH"
    created_at   timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, seq),
    CONSTRAINT run_point_srid_chk CHECK (ST_SRID(geom) = 5179)
    );

CREATE INDEX IF NOT EXISTS run_point_time_idx
    ON run_point(session_id, recorded_at);
CREATE INDEX IF NOT EXISTS run_point_gix
    ON run_point USING GIST (geom);

-- =========================================
-- run_route  (네이티브 UPSERT에서 updated_at=now()로 갱신)
-- =========================================
CREATE TABLE IF NOT EXISTS run_route (
    session_id        BIGINT PRIMARY KEY   REFERENCES run_session(session_id) ON DELETE CASCADE,
    route_geom        geometry(LineStringZ, 5179) NOT NULL,
    route_geom_simple geometry(LineStringZ, 5179),
    length_m          DOUBLE PRECISION,
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now()  -- 네이티브 UPSERT에서 now()로 갱신
    );

CREATE INDEX IF NOT EXISTS run_route_gix
    ON run_route USING GIST (route_geom);
CREATE INDEX IF NOT EXISTS run_route_simple_gix
    ON run_route USING GIST (route_geom_simple);