-- 필요 시 (PostGIS)
CREATE EXTENSION IF NOT EXISTS postgis;


-- =========================================
-- users (기존 그대로, 기본값 already OK)
-- =========================================
CREATE TABLE IF NOT EXISTS users (
    user_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    google_id                VARCHAR(255)  UNIQUE,
    email                    VARCHAR(100)  NOT NULL UNIQUE,
    nickname                 VARCHAR(50),
    profile_image_url        VARCHAR(500),

    password                VARCHAR(255),
    user_code               TEXT,

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
    start_time         timestamptz,
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
CREATE TABLE IF NOT EXISTS boutique_items (
    -- 기본 식별자
    -- 각 아이템을 구분하는 고유번호 (자동 증가, bigint)
    item_id BIGSERIAL PRIMARY KEY,
    -- 카테고리 및 식별 정보
    -- 아이템의 분류 (몸, 옷, 머리카락, 머리장식)
    category VARCHAR(20) NOT NULL CHECK (category IN ('bodies', 'clothes', 'hair', 'head')),
    --서브 카테고리
    subcategory VARCHAR(50) NOT NULL
    CHECK (subcategory IN (
    -- bodies
           'male',
    -- clothes
           'longsleeve', 'shorts', 'shortsleeves', 'tshirt',
    -- hair
           'afro', 'buzzcut', 'dreadlocks_long', 'long_hair', 'messy1', 'pixie',
    -- head
           'ears', 'eyebrows', 'eyes', 'faces', 'heads', 'nose'
                          )),
    --소분류
    style VARCHAR(50)
    CHECK (style IS NULL OR style IN (
    -- eyebrows styles
           'thick', 'thin',
    -- eyes styles
           'anger', 'closing', 'default_eye', 'eyeroll',
           'look_l', 'look_r', 'neutral', 'sad', 'sad2',
           'shame', 'shock',
    -- faces styles
           'anger', 'blush', 'closed', 'closing', 'eyeroll',
           'happy', 'look_l', 'look_r', 'neutral', 'sad',
           'shame', 'shock',
    -- ears styles
           'medium'
                                     )),
    --색상
    color VARCHAR(50),
    -- 아이템의 이름 (사용자에게 표시될 이름)
    name VARCHAR(100) NOT NULL,
    -- 실제 애셋 폴더명 또는 파일 식별 키 (중복 방지용)
    asset_key VARCHAR(100) NOT NULL,
    -- 애셋의 실제 경로 (예: AssetsStore/spritesheets/clothes/coat01.png)
    base_path VARCHAR(300) NOT NULL,
    -- 등급 및 가격
    -- 아이템 희귀도 (일반, 희귀, 에픽, 전설)
    rarity VARCHAR(20) CHECK (rarity IN ('common', 'rare', 'epic', 'legendary')),
    -- 상점 구매용 가격 (CR: CyberRun 화폐 단위)
    price_cr INT NOT NULL DEFAULT 0,
    -- 아이템 획득 경로 (가챠, 상점, 보상)
    obtain_method VARCHAR(20) CHECK (obtain_method IN ('gacha', 'store')),

    -- 생성·갱신 정보
    created_at timestamptz DEFAULT now(),  -- 생성 시간 (UTC 기반)
    updated_at timestamptz DEFAULT now(),  -- 수정 시간 (UTC 기반)

    -- 중복 방지 제약조건
    -- 동일 카테고리 내 동일 asset_key 중복 불가
    UNIQUE (category, subcategory, style, color, asset_key)
    );

-- [2] 등급별 확률 테이블
CREATE TABLE boutique_rarity_rates (
       rarity VARCHAR(20) PRIMARY KEY,
       probability NUMERIC(5,2) NOT NULL CHECK (probability >= 0 AND probability <= 100),
       description TEXT
);

INSERT INTO boutique_rarity_rates (rarity, probability, description) VALUES
     ('common', 60.00, '기본 의상 및 저레벨 파츠'),
     ('rare', 25.00, '희귀 파츠, 일반 뽑기에서 자주 등장'),
     ('epic', 10.00, '에픽 등급 의상, 러닝 보상형'),
     ('legendary', 5.00, '전설 등급, 한정판 혹은 이벤트 전용');

-- =========================================================
-- 🔍 인덱스
-- =========================================================

-- 카테고리별 탐색 속도 향상
CREATE INDEX IF NOT EXISTS idx_boutique_category ON boutique_items(category);
-- 희귀도별 탐색 속도 향상
CREATE INDEX IF NOT EXISTS idx_boutique_rarity ON boutique_items(rarity);
-- 획득경로별 탐색 속도 향상
CREATE INDEX IF NOT EXISTS idx_boutique_obtain ON boutique_items(obtain_method);
-- 가격대별 정렬 및 필터링 속도 향상
CREATE INDEX IF NOT EXISTS idx_boutique_price ON boutique_items(price_cr);


-- =========================================================
-- 🎯 유저가 실제로 ‘뽑기’를 수행한 내역 테이블
-- =========================================================

CREATE TABLE IF NOT EXISTS gacha_history (
     history_id BIGSERIAL PRIMARY KEY,        -- 고유 식별자
     user_id BIGINT NOT NULL,                 -- 뽑은 유저
     item_id BIGINT NOT NULL REFERENCES boutique_items(item_id) ON DELETE CASCADE,
    rarity VARCHAR(20) NOT NULL CHECK (rarity IN ('common', 'rare', 'epic', 'legendary')), -- 등급 (common, rare, epic, legendary)
    draw_type VARCHAR(20) DEFAULT 'single' CHECK (draw_type IN ('single', 'multi')), ,  -- 단일 / 10연 등 구분
     draw_time timestamptz DEFAULT now(),     -- 뽑은 시간
     session_id UUID DEFAULT gen_random_uuid() -- 10연차 단위 묶음
     );

-- =========================================================
-- 🎯 유저가 소유하고 있는 아이템들(중복 허용)
-- 중복 허용된 아이템의 처리의 경우 추후에 분해 시스템을 도입하는 등등 활용 가능
-- =========================================================

CREATE TABLE IF NOT EXISTS user_inventory (
        inventory_id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        item_id BIGINT NOT NULL REFERENCES boutique_items(item_id) ON DELETE CASCADE,
        quantity INT DEFAULT 1,                 -- 중복 보유 가능 시 카운트
        first_obtained_at timestamptz DEFAULT now(),
        last_obtained_at timestamptz DEFAULT now(),

    -- 중복 소유 허용 구조: 같은 아이템 여러 번 나오면 quantity 증가
    UNIQUE (user_id, item_id)
    );

-- =========================================================
-- 🎯 유저가 착용하고 있는 아이템들 파츠별로 하나씩
-- =========================================================

CREATE TABLE user_equipped_items (
     equipped_id BIGSERIAL PRIMARY KEY,
     user_id BIGINT NOT NULL REFERENCES users(user_id),
     item_id BIGINT NOT NULL REFERENCES boutique_items(item_id),
     category VARCHAR(20) NOT NULL CHECK (category IN ('bodies', 'clothes', 'hair', 'head')),
     subcategory VARCHAR(50) NOT NULL
         CHECK (subcategory IN (
                                'male',
                                'longsleeve', 'shorts', 'shortsleeves', 'tshirt',
                                'afro', 'buzzcut', 'dreadlocks_long', 'long_hair', 'messy1', 'pixie',
                                'ears', 'eyebrows', 'eyes', 'faces', 'heads', 'nose'
             )),
     style VARCHAR(50)
         CHECK (style IS NULL OR style IN (
                                           'thick', 'thin',
                                           'anger', 'closing', 'default_eye', 'eyeroll',
                                           'look_l', 'look_r', 'neutral', 'sad', 'sad2',
                                           'shame', 'shock',
                                           'blush', 'closed', 'happy',
                                           'medium'
             )),
     equipped_at timestamptz DEFAULT now(),

    -- 한 슬롯(카테고리+서브카테고리)에는 1개만 착용 가능
     UNIQUE (user_id, category, subcategory)
);

-- =========================================
-- 사용자 온보딩 선호도 설정 테이블
-- =========================================
CREATE TABLE IF NOT EXISTS user_preferences (
    preference_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    
    -- 온보딩 필수 입력 항목
    has_running_history BOOLEAN NOT NULL,
    fitness_level VARCHAR(20) NOT NULL,
    target_distance_km REAL NOT NULL,
    resting_heart_rate INT,
    has_smart_watch BOOLEAN NOT NULL,
    
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_user_preferences_user 
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT user_preferences_fitness_level_check CHECK (
        fitness_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT', 'ELITE')
    ),
    CONSTRAINT user_preferences_resting_heart_rate_check CHECK (
        resting_heart_rate IS NULL OR (resting_heart_rate BETWEEN 40 AND 120)
    ),
    CONSTRAINT user_preferences_target_distance_check CHECK (
        target_distance_km >= 1.0 AND target_distance_km <= 40.0
    )
);

-- user_preferences updated_at 자동 갱신 트리거
DROP TRIGGER IF EXISTS trg_user_preferences_updated_at ON user_preferences;
CREATE TRIGGER trg_user_preferences_updated_at
    BEFORE UPDATE ON user_preferences
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- =========================================
-- 친구 관계 테이블 (friendship)
-- =========================================
CREATE TABLE IF NOT EXISTS friendship (
    friendship_id BIGSERIAL PRIMARY KEY,
    
    requester_id BIGINT NOT NULL,  -- 요청 보낸 사람
    addressee_id BIGINT NOT NULL,  -- 요청 받은 사람
    
    -- 상태: PENDING(대기), ACCEPTED(수락), REJECTED(거절), CANCELLED(취소), BLOCKED(차단)
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED', 'BLOCKED')),
    
    created_at timestamptz NOT NULL DEFAULT now(), -- 생성 시간
    updated_at timestamptz NOT NULL DEFAULT now(), -- 수정 시간
    
    CONSTRAINT fk_friendship_requester 
        FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_addressee 
        FOREIGN KEY (addressee_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- 자기 자신과 친구 요청 방지
    CONSTRAINT friendship_no_self_request 
        CHECK (requester_id != addressee_id),
    
    -- 중복 요청 방지: 같은 사람에게 중복 요청 불가
    CONSTRAINT friendship_unique_request 
        UNIQUE (requester_id, addressee_id)
);

CREATE INDEX IF NOT EXISTS idx_friendship_requester_status 
    ON friendship(requester_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_friendship_addressee_status 
    ON friendship(addressee_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_friendship_accepted_requester 
    ON friendship(requester_id) WHERE status = 'ACCEPTED';
CREATE INDEX IF NOT EXISTS idx_friendship_accepted_addressee 
    ON friendship(addressee_id) WHERE status = 'ACCEPTED';

DROP TRIGGER IF EXISTS trg_friendship_updated_at ON friendship;
CREATE TRIGGER trg_friendship_updated_at
    BEFORE UPDATE ON friendship
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TABLE run_ai_report (
                               id          BIGSERIAL PRIMARY KEY,
                               session_id  BIGINT NOT NULL UNIQUE
                                   REFERENCES run_session(session_id) ON DELETE CASCADE,
                               content     TEXT NOT NULL,
                               model       VARCHAR(50) NOT NULL,
                               created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 유저 태그
CREATE TABLE IF NOT EXISTS user_tags (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_user_tag UNIQUE (user_id, tag_name)
);

-- 공개범위 설정
CREATE TABLE IF NOT EXISTS privacy_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    is_global_public BOOLEAN NOT NULL DEFAULT TRUE,
    show_total_running BOOLEAN NOT NULL DEFAULT TRUE,
    show_max_distance BOOLEAN NOT NULL DEFAULT TRUE,
    show_avg_pace BOOLEAN NOT NULL DEFAULT TRUE,
    show_best_pace BOOLEAN NOT NULL DEFAULT TRUE,
    show_hiking_count BOOLEAN NOT NULL DEFAULT TRUE
);



--  weekly_mission 테이블
CREATE TABLE IF NOT EXISTS weekly_mission (
  weekly_mission_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  user_id        BIGINT         NOT NULL,
  week_start     TIMESTAMPTZ    NOT NULL,             -- 주 시작(월요일 00:00 KST)
  target_days    INT            NOT NULL DEFAULT 5,   -- 해당 주 완료해야 할 일일미션 수
  completed_days INT            NOT NULL DEFAULT 0,   -- 실제 완료 일수(캐시)
  completed      BOOLEAN        NOT NULL DEFAULT FALSE,
  claimed        BOOLEAN        NOT NULL DEFAULT FALSE,
  created_at     TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),

  CONSTRAINT uk_weekly_mission_user_week UNIQUE (user_id, week_start),
  CONSTRAINT ck_weekly_mission_nonneg CHECK (target_days >= 0 AND completed_days >= 0),
  CONSTRAINT fk_weekly_mission_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_weekly_mission_user_week
  ON weekly_mission (user_id, week_start);
