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


-- =========================================
-- 1) run_session
--  - status 없음: end_time NULL 여부로 진행/완료 판단
--  - summary.*: 클라 JSON과 이름/타입 1:1
--  - created_at / updated_at: JPA Auditing (앱)에서 세팅
--  - 멱등: (client_secret_key, user_id, start_time)
-- =========================================
CREATE TABLE IF NOT EXISTS run_session (
                                           session_id        BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                           user_id           TEXT         NOT NULL,                 -- from userId (String)
                                           client_secret_key TEXT,                                  -- from clientSecretKey

                                           type              TEXT         NOT NULL CHECK (type IN ('NORMAL','ENTRY')),
    base_id           BIGINT,
    device_type       TEXT         NOT NULL CHECK (device_type IN ('PHONE','WATCH')),

    -- 시간 (클라 ms → timestamptz 변환 저장)
    start_time        timestamptz  NOT NULL,                 -- from startTime(ms)
    end_time          timestamptz,                           -- from endTime(ms)

-- ===== summary (이름/타입 100% 일치) =====
    total_steps       INTEGER,                                -- int
    total_distance    DOUBLE PRECISION,                       -- Double (meters)
    total_calories    INTEGER,                                -- int (kcal)
    avg_heart_rate    INTEGER,                                -- int (bpm)
    duration          INTEGER,                                -- int (seconds)
    avg_cadence       INTEGER,                                -- int (spm)
    avg_pace          INTEGER,                                -- int (sec/km)
    elevation         DOUBLE PRECISION,                       -- Double (meters)

-- 원본 JSON 보관
    cadence_records    JSONB,                                 -- [ {seq, cadence, caloriesIncrement, createdAt}, ... ]
    heart_rate_records JSONB,                                 -- [ {seq, heartRate, createdAt}, ... ]

-- 보상/게임 결과 메타
    rewards_meta       JSONB,

    -- 사후 동기화 멱등
    UNIQUE (client_secret_key, user_id, start_time),

    -- 앱(Auditing)이 채움
    created_at        timestamptz  NOT NULL,
    updated_at        timestamptz  NOT NULL
    );

-- 조회 보조 인덱스(선택)
CREATE INDEX IF NOT EXISTS run_session_user_time_idx ON run_session (user_id, start_time DESC);
CREATE INDEX IF NOT EXISTS run_session_end_time_idx  ON run_session (end_time);

-- =========================================
-- 2) gps_points — 포인트별 저장 (권장)
--  - created_at: 포인트 측정 시각 (클라 createdAt ms → timestamptz)
--  - geom: PointZ(5179) (lon,lat,alt → 4326→5179 변환)
--  - speed: Float → REAL (m/s)
--  - row_created_at: DB 행 기록 시각(감사용, default now())
-- =========================================
CREATE TABLE IF NOT EXISTS gps_points (
                                          session_id     BIGINT NOT NULL REFERENCES run_session(session_id) ON DELETE CASCADE,
    seq            INTEGER NOT NULL CHECK (seq > 0),

    created_at     timestamptz NOT NULL,                    -- from createdAt(ms)
    geom           geometry(PointZ, 5179) NOT NULL,         -- X=lon, Y=lat, Z=alt
    speed          REAL,                                     -- m/s

    row_created_at timestamptz NOT NULL DEFAULT now(),

    PRIMARY KEY (session_id, seq),
    CONSTRAINT gps_points_srid_chk CHECK (ST_SRID(geom) = 5179)
    );

CREATE INDEX IF NOT EXISTS gps_points_time_idx ON gps_points(session_id, created_at);
CREATE INDEX IF NOT EXISTS gps_points_gix      ON gps_points USING GIST (geom);

-- =========================================
-- 3) run_route — 경로 라인 요약
--  - route_geom: 원본 라인 (정밀 계산용)
--  - route_geom_simple: 단순화 라인(표시/썸네일/저배율용)
--  - length_m: ST_Length(route_geom)
--  - created_at / updated_at: 앱(Auditing)에서 세팅
-- =========================================
CREATE TABLE IF NOT EXISTS run_route (
                                         session_id        BIGINT PRIMARY KEY REFERENCES run_session(session_id) ON DELETE CASCADE,
    route_geom        geometry(LineStringZ, 5179) NOT NULL,
    route_geom_simple geometry(LineStringZ, 5179),
    length_m          DOUBLE PRECISION,

    created_at        timestamptz NOT NULL,
    updated_at        timestamptz NOT NULL
    );

CREATE INDEX IF NOT EXISTS run_route_gix        ON run_route USING GIST (route_geom);
CREATE INDEX IF NOT EXISTS run_route_simple_gix ON run_route USING GIST (route_geom_simple);
