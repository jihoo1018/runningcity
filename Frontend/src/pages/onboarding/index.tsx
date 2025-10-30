// src/pages/onboarding/index.tsx

import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { completeOnboarding, FitnessLevel } from '../../shared/api/onboarding';

const OnboardingPage = () => {
  const navigate = useNavigate();
  
  // TODO: 실제로는 로그인된 사용자 ID를 가져와야 함
  const userId = 1;

  const [hasRunningHistory, setHasRunningHistory] = useState<boolean | null>(null);
  const [targetDistance, setTargetDistance] = useState('');
  const [fitnessLevel, setFitnessLevel] = useState<FitnessLevel | ''>('');
  const [restingHeartRate, setRestingHeartRate] = useState('');
  const [hasSmartWatch, setHasSmartWatch] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    // 유효성 검사
    if (hasRunningHistory === null) {
      setError('러닝 이력을 선택해주세요');
      return;
    }

    if (!targetDistance || parseFloat(targetDistance) < 1 || parseFloat(targetDistance) > 40) {
      setError('목표 거리는 1~40km 사이로 입력해주세요');
      return;
    }

    if (!fitnessLevel) {
      setError('운동 능력치를 선택해주세요');
      return;
    }

    if (restingHeartRate && (parseInt(restingHeartRate) < 40 || parseInt(restingHeartRate) > 120)) {
      setError('심박수는 40~120 사이로 입력해주세요');
      return;
    }

    setIsLoading(true);

    // 백그라운드로 API 호출 (실패해도 페이지 이동)
    const onboardingData: any = {
      hasRunningHistory,
      fitnessLevel,
      targetDistanceKm: parseFloat(targetDistance),
      hasSmartWatch,
    };
    
    if (restingHeartRate) {
      onboardingData.restingHeartRate = parseInt(restingHeartRate);
    }
    
    completeOnboarding(userId, onboardingData).catch((err) => {
      console.error('온보딩 실패:', err);
    });
    
    // 짧은 딜레이 후 홈으로 이동
    setTimeout(() => {
      setIsLoading(false);
      navigate('/');
    }, 100);
  };

  const fitnessOptions: { value: FitnessLevel; label: string }[] = [
    { value: 'BEGINNER', label: '입문자 (처음 시작)' },
    { value: 'INTERMEDIATE', label: '초급자 (가끔 운동)' },
    { value: 'ADVANCED', label: '중급자 (주 2-3회)' },
    { value: 'EXPERT', label: '상급자 (주 4-5회)' },
    { value: 'ELITE', label: '전문가 (매일 운동)' },
  ];

  return (
    <div
      style={{
        width: '100vw',
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#e5e5e5',
        padding: '20px',
        boxSizing: 'border-box',
      }}
    >
      <div
        style={{
          width: '100%',
          maxWidth: '400px',
          backgroundColor: '#d4d4d4',
          border: '2px solid #9ca3af',
          borderRadius: '8px',
          padding: '30px 25px',
          display: 'flex',
          flexDirection: 'column',
          gap: '20px',
        }}
      >
        {/* 헤더 */}
        <div style={{ textAlign: 'center', paddingBottom: '10px', borderBottom: '2px solid #9ca3af' }}>
          <h1
            style={{
              fontSize: '24px',
              fontWeight: 'normal',
              color: '#4b5563',
              margin: '0 0 8px 0',
              fontFamily: 'Arial, sans-serif',
            }}
          >
            [ 온보딩 정보 입력 ]
          </h1>
          <p
            style={{
              fontSize: '13px',
              color: '#6b7280',
              margin: 0,
              fontFamily: 'Arial, sans-serif',
            }}
          >
            러닝 활동에 필요한 정보를 입력해주세요
          </p>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* 러닝 이력 */}
          <div>
            <label
              style={{
                display: 'block',
                fontSize: '12px',
                color: '#4b5563',
                marginBottom: '10px',
                fontFamily: 'Arial, sans-serif',
                fontWeight: 'bold',
              }}
            >
              러닝 이력 T/F
            </label>
            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                type="button"
                onClick={() => setHasRunningHistory(true)}
                style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '14px',
                  color: hasRunningHistory === true ? '#1f2937' : '#6b7280',
                  backgroundColor: hasRunningHistory === true ? '#ffffff' : '#e5e7eb',
                  border: hasRunningHistory === true ? '2px solid #6b7280' : '2px solid #9ca3af',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontFamily: 'Arial, sans-serif',
                }}
              >
                [ True ]
              </button>
              <button
                type="button"
                onClick={() => setHasRunningHistory(false)}
                style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '14px',
                  color: hasRunningHistory === false ? '#1f2937' : '#6b7280',
                  backgroundColor: hasRunningHistory === false ? '#ffffff' : '#e5e7eb',
                  border: hasRunningHistory === false ? '2px solid #6b7280' : '2px solid #9ca3af',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontFamily: 'Arial, sans-serif',
                }}
              >
                [ False ]
              </button>
            </div>
          </div>

          {/* 목표 거리 */}
          <div>
            <label
              style={{
                display: 'block',
                fontSize: '12px',
                color: '#4b5563',
                marginBottom: '8px',
                fontFamily: 'Arial, sans-serif',
                fontWeight: 'bold',
              }}
            >
              목표 km
            </label>
            <input
              type="number"
              value={targetDistance}
              onChange={(e) => setTargetDistance(e.target.value)}
              placeholder="목표 거리 (1~40km)"
              min="1"
              max="40"
              step="0.1"
              style={{
                width: '100%',
                padding: '12px',
                fontSize: '14px',
                border: '2px solid #9ca3af',
                borderRadius: '4px',
                outline: 'none',
                boxSizing: 'border-box',
                backgroundColor: '#ffffff',
                fontFamily: 'Arial, sans-serif',
              }}
            />
          </div>

          {/* 운동 능력치 */}
          <div>
            <label
              style={{
                display: 'block',
                fontSize: '12px',
                color: '#4b5563',
                marginBottom: '8px',
                fontFamily: 'Arial, sans-serif',
                fontWeight: 'bold',
              }}
            >
              현재 운동 가능 능력치
            </label>
            <select
              value={fitnessLevel}
              onChange={(e) => setFitnessLevel(e.target.value as FitnessLevel)}
              style={{
                width: '100%',
                padding: '12px',
                fontSize: '14px',
                border: '2px solid #9ca3af',
                borderRadius: '4px',
                outline: 'none',
                boxSizing: 'border-box',
                backgroundColor: '#ffffff',
                fontFamily: 'Arial, sans-serif',
              }}
            >
              <option value="">선택해주세요</option>
              {fitnessOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          {/* 심박수 측정 */}
          <div>
            <label
              style={{
                display: 'block',
                fontSize: '12px',
                color: '#4b5563',
                marginBottom: '8px',
                fontFamily: 'Arial, sans-serif',
                fontWeight: 'bold',
              }}
            >
              측정치 (선택사항)
            </label>
            <div style={{ display: 'flex', gap: '10px' }}>
              <input
                type="number"
                value={restingHeartRate}
                onChange={(e) => setRestingHeartRate(e.target.value)}
                placeholder="심박수 (40~120)"
                min="40"
                max="120"
                disabled={!hasSmartWatch}
                style={{
                  flex: 1,
                  padding: '12px',
                  fontSize: '14px',
                  border: '2px solid #9ca3af',
                  borderRadius: '4px',
                  outline: 'none',
                  boxSizing: 'border-box',
                  backgroundColor: !hasSmartWatch ? '#e5e7eb' : '#ffffff',
                  fontFamily: 'Arial, sans-serif',
                }}
              />
              <button
                type="button"
                disabled={!hasSmartWatch}
                style={{
                  padding: '12px 20px',
                  fontSize: '14px',
                  color: !hasSmartWatch ? '#9ca3af' : '#1f2937',
                  backgroundColor: !hasSmartWatch ? '#e5e7eb' : '#a3a3a3',
                  border: '2px solid #6b7280',
                  borderRadius: '4px',
                  cursor: !hasSmartWatch ? 'not-allowed' : 'pointer',
                  fontFamily: 'Arial, sans-serif',
                  whiteSpace: 'nowrap',
                }}
              >
                [ 심박수 측정 ]
              </button>
            </div>
          </div>

          {/* 워치 체크박스 */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              padding: '10px',
            }}
          >
            <label
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                cursor: 'pointer',
                fontSize: '13px',
                color: '#4b5563',
                fontFamily: 'Arial, sans-serif',
              }}
            >
              <input
                type="checkbox"
                checked={!hasSmartWatch}
                onChange={(e) => {
                  setHasSmartWatch(!e.target.checked);
                  if (e.target.checked) {
                    setRestingHeartRate('');
                  }
                }}
                style={{
                  width: '18px',
                  height: '18px',
                  cursor: 'pointer',
                }}
              />
              워치가 없어요 체크
            </label>
          </div>

          {/* 에러 메시지 */}
          {error && (
            <div
              style={{
                fontSize: '12px',
                color: '#dc2626',
                backgroundColor: '#fee2e2',
                padding: '10px',
                borderRadius: '4px',
                border: '1px solid #dc2626',
                fontFamily: 'Arial, sans-serif',
              }}
            >
              ⚠ {error}
            </div>
          )}

          {/* 제출 버튼 */}
          <button
            type="submit"
            disabled={isLoading}
            style={{
              width: '100%',
              padding: '16px',
              fontSize: '15px',
              fontWeight: 'bold',
              color: isLoading ? '#6b7280' : '#1f2937',
              backgroundColor: isLoading ? '#e5e7eb' : '#a3a3a3',
              border: '2px solid #6b7280',
              borderRadius: '4px',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              fontFamily: 'Arial, sans-serif',
            }}
          >
            {isLoading ? '[ 제출 중... ]' : '[ 제출하기 ]'}
          </button>
        </form>

        {/* 안내 문구 */}
        <div
          style={{
            fontSize: '11px',
            color: '#6b7280',
            textAlign: 'left',
            paddingTop: '12px',
            borderTop: '1px dashed #9ca3af',
            fontFamily: 'Arial, sans-serif',
            lineHeight: '1.6',
          }}
        >
          * 모든 정보는 맞춤형 러닝 추천에 사용됩니다<br />
          * 심박수는 워치가 있을 때만 입력 가능합니다
        </div>
      </div>
    </div>
  );
};

export default OnboardingPage;

