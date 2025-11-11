INSERT INTO users (
    google_id,
    email,
    nickname,
    profile_image_url,
    password,
    user_code,
    has_completed_onboarding,
    level,
    total_exp,
    total_credit,
    is_active,
    created_at,
    updated_at
) VALUES (
     'google_test_123456789',
     'testuser@runningcity.com',
     '테스트러너',
     'https://via.placeholder.com/150',
     null,  -- 구글 로그인이므로 password null
     'RC001',  -- 유저 코드
     false,  -- 온보딩 미완료
     1,  -- 레벨 1
     0,  -- 경험치 0
     10000,  -- 초기 크레딧 10,000 (테스트용)
     true,  -- 활성 계정
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP
 ) ON CONFLICT (google_id) DO NOTHING;
-- 온보딩 완료 유저 (고레벨)
INSERT INTO users (
    google_id,
    email,
    nickname,
    profile_image_url,
    password,
    user_code,
    has_completed_onboarding,
    level,
    total_exp,
    total_credit,
    is_active
) VALUES (
     'google_test_987654321',
     'runner_pro@runningcity.com',
     '프로러너',
     'https://via.placeholder.com/150',
     null,
     'RC002',
     true,  -- ✅ 온보딩 완료
     15,  -- 레벨 15
     25000,  -- 경험치 25,000
     50000,  -- 크레딧 50,000
     true
 ) ON CONFLICT (google_id) DO NOTHING;

-- 크레딧 부족 유저
INSERT INTO users (
    google_id,
    email,
    nickname,
    profile_image_url,
    password,
    user_code,
    has_completed_onboarding,
    level,
    total_exp,
    total_credit,
    is_active
) VALUES (
     'google_test_111222333',
     'newbie@runningcity.com',
     '초보러너',
     null,
     null,
     'RC003',
     true,
     3,
     1500,
     100,  -- ⚠️ 크레딧 부족
     true
 ) ON CONFLICT (google_id) DO NOTHING;