INSERT INTO users (
    user_id, 
    google_id, 
    email, 
    nickname, 
    profile_image_url, 
    has_completed_onboarding, 
    level, 
    total_running_energy, 
    is_active, 
    created_at, 
    updated_at
) VALUES (
    1,
    'google_test_123456789',
    'testuser@runningcity.com',
    null,
    null,
    false,
    1,
    0,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (user_id) DO NOTHING;