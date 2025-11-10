// src/pages/nickname/index.tsx

import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { updateNickname } from '../../shared/api/nickname';

const NicknamePage = () => {
  const navigate = useNavigate();
  const [nickname, setNickname] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // TODO: 실제로는 로그인된 사용자 ID를 가져와야 함
  const userId = 9; // 임시 하드코딩

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    
    // 닉네임 유효성 검사
    if (!nickname.trim()) {
      setError('닉네임을 입력해 주세요');
      return;
    }

    if (nickname.length < 2) {
      setError('닉네임은 2글자 이상이어야 합니다');
      return;
    }

    if (nickname.length > 10) {
      setError('닉네임은 10글자 이하여야 합니다');
      return;
    }

    // 한글, 영문, 숫자만 허용
    const nicknamePattern = /^[a-zA-Z0-9가-힣]+$/;
    if (!nicknamePattern.test(nickname)) {
      setError('닉네임은 한글, 영문, 숫자만 사용 가능합니다');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const result = await updateNickname(userId, nickname.trim());
      console.log('닉네임 업데이트 완료:', result);
      
      // 성공 시 온보딩 페이지로 이동
      navigate('/onboarding');
    } catch (err: any) {
      setIsLoading(false);
      
      // API 에러 응답 처리
      if (err.response?.data) {
        const errorData = err.response.data;
        
        // 닉네임 중복 (409)
        if (errorData.code === 'USER_4090') {
          setError('이미 사용 중인 닉네임입니다. 다른 닉네임을 입력해주세요.');
          return;
        }
        
        // 유효성 검증 실패 (400)
        if (errorData.code === 'USER_4000' && errorData.error?.details) {
          const fieldErrors = errorData.error.details
            .map((detail: any) => detail.message)
            .join(', ');
          setError(fieldErrors);
          return;
        }
        
        // 기타 에러
        setError(errorData.message || '닉네임 업데이트 중 오류가 발생했습니다.');
      } else {
        setError('네트워크 오류가 발생했습니다. 다시 시도해주세요.');
      }
      
      console.error('닉네임 업데이트 실패:', err);
    }
  };

  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        minWidth: '100vw',
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#e5e5e5',
        padding: '20px',
        boxSizing: 'border-box',
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
      }}
    >
      <div
        style={{
          width: '100%',
          maxWidth: '400px',
          backgroundColor: '#d4d4d4',
          border: '2px solid #9ca3af',
          borderRadius: '8px',
          padding: '40px 30px',
          display: 'flex',
          flexDirection: 'column',
          gap: '24px',
        }}
      >
        {/* 헤더 */}
        <div style={{ textAlign: 'center' }}>
          <h1
            style={{
              fontSize: '25px',
              fontWeight: 'normal',
              color: '#4b5563',
              margin: '0 0 8px 0',
              fontFamily: 'Arial, sans-serif',
            }}
          >
            [ 러닝시티: 러너즈 프로토콜 ]
          </h1>
          <p
            style={{
              fontSize: '14px',
              color: '#6b7280',
              margin: 0,
              fontFamily: 'Arial, sans-serif',
            }}
          >
            닉네임을 입력해 주세요
          </p>
        </div>

        {/* 입력 폼 */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label
              style={{
                display: 'block',
                fontSize: '12px',
                color: '#6b7280',
                marginBottom: '8px',
                fontFamily: 'Arial, sans-serif',
              }}
            >
              닉네임
            </label>
            <input
              type="text"
              value={nickname}
              onChange={(e) => {
                setNickname(e.target.value);
                setError(''); // 입력 시 에러 초기화
              }}
              placeholder="닉네임을 입력해 주세요"
              maxLength={10}
              disabled={isLoading}
              style={{
                width: '100%',
                padding: '12px',
                fontSize: '14px',
                border: error ? '2px solid #ef4444' : '2px solid #9ca3af',
                borderRadius: '4px',
                outline: 'none',
                boxSizing: 'border-box',
                backgroundColor: isLoading ? '#e5e7eb' : '#ffffff',
                fontFamily: 'Arial, sans-serif',
              }}
            />
            {/* 글자수 표시 */}
            <div
              style={{
                fontSize: '11px',
                color: '#9ca3af',
                textAlign: 'right',
                marginTop: '4px',
                fontFamily: 'Arial, sans-serif',
              }}
            >
              {nickname.length}/10자
            </div>
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

          {/* 확인 버튼 */}
          <button
            type="submit"
            disabled={isLoading || !nickname.trim()}
            style={{
              width: '100%',
              padding: '14px',
              fontSize: '14px',
              fontWeight: 'normal',
              color: isLoading || !nickname.trim() ? '#6b7280' : '#1f2937',
              backgroundColor: isLoading || !nickname.trim() ? '#e5e7eb' : '#a3a3a3',
              border: '2px solid #6b7280',
              borderRadius: '4px',
              cursor: isLoading || !nickname.trim() ? 'not-allowed' : 'pointer',
              minHeight: '48px',
              fontFamily: 'Arial, sans-serif',
            }}
          >
            {isLoading ? '[ 처리중... ]' : '[ 확인 ]'}
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
          * 한글, 영문, 숫자만 사용 가능<br />
          * 2~10자 입력
        </div>
      </div>
    </div>
  );
};

export default NicknamePage;

