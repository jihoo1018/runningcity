-- 필요 시 (PostGIS)
CREATE EXTENSION IF NOT EXISTS postgis;


-- =========================================
-- users (기존 그대로, 기본값 already OK)
-- =========================================
CREATE TABLE IF NOT EXISTS users (
                                     user_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     google_id                VARCHAR(255)  NOT NULL UNIQUE,
    email                    VARCHAR(100)  NOT NULL UNIQUE,
    nickname                 VARCHAR(50),
    profile_image_url        VARCHAR(500),

    has_completed_onboarding BOOLEAN       NOT NULL DEFAULT FALSE,
    level                    INTEGER       NOT NULL DEFAULT 1,
    total_exp                BIGINT        NOT NULL DEFAULT 0,
    total_credit             BIGINT        NOT NULL DEFAULT 0,
    is_active                BOOLEAN       NOT NULL DEFAULT TRUE,

    created_at               timestamptz   NOT NULL DEFAULT now(),
    updated_at               timestamptz   NOT NULL DEFAULT now()
    );

-- =========================================
-- 1) run_session
--  - created_at/updated_at: 기본값 now() + UPDATE 트리거
--  - 무결성: end_time IS NULL OR end_time >= start_time
--  - 멱등: (user_id, client_secret_key) ← client_secret_key 있을 때만 (UNIQUE에서 NULL은 중복 허용)
-- =========================================
CREATE TABLE IF NOT EXISTS run_session (
                                           session_id         BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                           user_id            BIGINT       NOT NULL,                 -- FK → users.user_id
                                           client_secret_key  TEXT,                                  -- from clientSecretKey

                                           type               TEXT         NOT NULL CHECK (type IN ('NORMAL','ENTRY')),
    base_id            BIGINT,
    device_type        TEXT         NOT NULL CHECK (device_type IN ('PHONE','WATCH')),

    -- 시간 (클라 ms → timestamptz 변환 저장)
    start_time         timestamptz,                           -- ← NULL 허용 (보정 가능)
    end_time           timestamptz,

    -- ===== summary (이름/타입 100% 일치) =====
    total_steps        INTEGER,
    total_distance     DOUBLE PRECISION,
    total_calories     INTEGER,
    avg_heart_rate     INTEGER,
    duration           INTEGER,
    avg_cadence        INTEGER,
    avg_pace           INTEGER,
    elevation          DOUBLE PRECISION,

    -- 원본 JSON 보관
    cadence_records     JSONB,
    heart_rate_records  JSONB,

    -- 보상/게임 결과 메타
    rewards_meta        JSONB,

    -- 생성/수정 시간: 기본값 now()
    created_at         timestamptz  NOT NULL DEFAULT now(),
    updated_at         timestamptz  NOT NULL DEFAULT now(),

    -- FK (inline)
    CONSTRAINT fk_run_session_user
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,

    -- 시간 무결성
    CONSTRAINT run_session_time_chk
    CHECK (end_time IS NULL OR end_time >= start_time),

    -- 워치 멱등: client_secret_key 있을 때만 사실상 유니크 (UNIQUE에서 NULL은 다중 허용)
    CONSTRAINT run_session_user_clientkey_uk UNIQUE (user_id, client_secret_key)
    );

-- 조회 보조 인덱스
CREATE INDEX IF NOT EXISTS run_session_user_time_idx ON run_session (user_id, start_time DESC);
CREATE INDEX IF NOT EXISTS run_session_end_time_idx  ON run_session (end_time);

-- =========================================
-- 2) gps_points
-- =========================================
CREATE TABLE IF NOT EXISTS gps_points (
                                          session_id      BIGINT NOT NULL REFERENCES run_session(session_id) ON DELETE CASCADE,
    seq             INTEGER NOT NULL CHECK (seq > 0),

    created_at      timestamptz NOT NULL,
    geom            geometry(PointZ, 5179) NOT NULL,
    speed           REAL,

    row_created_at  timestamptz NOT NULL DEFAULT now(),

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
                                         session_id         BIGINT PRIMARY KEY REFERENCES run_session(session_id) ON DELETE CASCADE,
    route_geom         geometry(LineStringZ, 5179) NOT NULL,
    route_geom_simple  geometry(LineStringZ, 5179),
    length_m           DOUBLE PRECISION,

    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now()
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

-- =========================================
-- entry (잠입 기지)
-- =========================================
CREATE TABLE IF NOT EXISTS entry (
     base_id        BIGSERIAL PRIMARY KEY,   -- JPA @Id + GenerationType.IDENTITY
     course_nm      VARCHAR(255),            -- 코스명
     course_desc    TEXT,                    -- 코스 설명
     region         VARCHAR(100),            -- 지역
     distance_km    DOUBLE PRECISION,        -- 거리 km
     difficulty     VARCHAR(50),             -- 난이도
     duration       VARCHAR(50),             -- 소요시간
     address        VARCHAR(255),            -- 주소
     latitude       DOUBLE PRECISION,        -- 위도
     longitude      DOUBLE PRECISION,        -- 경도
     data_source    VARCHAR(100),            -- 데이터 출처
     group_no       INTEGER,                 -- 그룹 번호

     created_at     timestamptz DEFAULT now(),  -- 생성 시간
     updated_at     timestamptz DEFAULT now()   -- 수정 시간
    );

-- 인덱스: 지역별/그룹별/좌표 검색 속도 향상용
CREATE INDEX IF NOT EXISTS entry_region_idx   ON entry(region);
CREATE INDEX IF NOT EXISTS entry_group_idx    ON entry(group_no);
CREATE INDEX IF NOT EXISTS entry_latlon_idx   ON entry(latitude, longitude);

-- updated_at 자동 갱신 트리거 (run_session과 동일 패턴)
DROP TRIGGER IF EXISTS trg_entry_updated_at ON entry;

CREATE TRIGGER trg_entry_updated_at
    BEFORE UPDATE ON entry
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- =========================================
-- 부티크(상점 , 뽑기) 관련
-- =========================================
