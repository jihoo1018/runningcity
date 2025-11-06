-- 필요 시 (PostGIS)
CREATE EXTENSION IF NOT EXISTS postgis;

-- =========================================
-- users (기존 그대로, 기본값 already OK)
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

-- =========================================
-- 1) run_session
--  - created_at/updated_at: 기본값 now() + UPDATE 트리거
--  - 무결성: end_time IS NULL OR end_time >= start_time
--  - 멱등: (client_secret_key, user_id, start_time)
-- =========================================
CREATE TABLE IF NOT EXISTS run_session (
    session_id        BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id           BIGINT       NOT NULL,                 -- FK → users.user_id
    client_secret_key TEXT,                                  -- from clientSecretKey

    type              TEXT         NOT NULL CHECK (type IN ('NORMAL','ENTRY')),
    base_id           BIGINT,
    device_type       TEXT         NOT NULL CHECK (device_type IN ('PHONE','WATCH')),

    -- 시간 (클라 ms → timestamptz 변환 저장)
    start_time        timestamptz  NOT NULL,
    end_time          timestamptz,

    -- ===== summary (이름/타입 100% 일치) =====
    total_steps       INTEGER,
    total_distance    DOUBLE PRECISION,
    total_calories    INTEGER,
    avg_heart_rate    INTEGER,
    duration          INTEGER,
    avg_cadence       INTEGER,
    avg_pace          INTEGER,
    elevation         DOUBLE PRECISION,

    -- 원본 JSON 보관
    cadence_records    JSONB,
    heart_rate_records JSONB,

    -- 보상/게임 결과 메타
    rewards_meta       JSONB,

    -- 멱등 (사후 동기화)
    UNIQUE (client_secret_key, user_id, start_time),

    -- 생성/수정 시간: 기본값 now()
    created_at        timestamptz  NOT NULL DEFAULT now(),
    updated_at        timestamptz  NOT NULL DEFAULT now(),

    -- FK (inline)
    CONSTRAINT fk_run_session_user
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,

    -- 시간 무결성
    CONSTRAINT run_session_time_chk
    CHECK (end_time IS NULL OR end_time >= start_time)
    );

-- 조회 보조 인덱스
CREATE INDEX IF NOT EXISTS run_session_user_time_idx ON run_session (user_id, start_time DESC);
CREATE INDEX IF NOT EXISTS run_session_end_time_idx  ON run_session (end_time);

-- =========================================
-- 2) gps_points
-- =========================================
CREATE TABLE IF NOT EXISTS gps_points (
    session_id     BIGINT NOT NULL REFERENCES run_session(session_id) ON DELETE CASCADE,
    seq            INTEGER NOT NULL CHECK (seq > 0),

    created_at     timestamptz NOT NULL,
    geom           geometry(PointZ, 5179) NOT NULL,
    speed          REAL,

    row_created_at timestamptz NOT NULL DEFAULT now(),

    PRIMARY KEY (session_id, seq),
    CONSTRAINT gps_points_srid_chk CHECK (ST_SRID(geom) = 5179)
    );

CREATE INDEX IF NOT EXISTS gps_points_time_idx ON gps_points(session_id, created_at);
CREATE INDEX IF NOT EXISTS gps_points_gix      ON gps_points USING GIST (geom);

-- =========================================
-- 3) run_route
--  - created_at/updated_at: 기본값 now() + UPDATE 트리거
-- =========================================
CREATE TABLE IF NOT EXISTS run_route (
    session_id        BIGINT PRIMARY KEY REFERENCES run_session(session_id) ON DELETE CASCADE,
    route_geom        geometry(LineStringZ, 5179) NOT NULL,
    route_geom_simple geometry(LineStringZ, 5179),
    length_m          DOUBLE PRECISION,

    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS run_route_gix        ON run_route USING GIST (route_geom);
CREATE INDEX IF NOT EXISTS run_route_simple_gix ON run_route USING GIST (route_geom_simple);

-- =========================================
-- updated_at 자동 갱신 트리거 (1번 정의, 2개 테이블에 부착)
-- =========================================
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at := now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_run_session_updated_at ON run_session;
CREATE TRIGGER trg_run_session_updated_at
    BEFORE UPDATE ON run_session
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_run_route_updated_at ON run_route;
CREATE TRIGGER trg_run_route_updated_at
    BEFORE UPDATE ON run_route
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
