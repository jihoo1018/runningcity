// src/pages/nickname/index.tsx

import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { updateNickname } from '@/entities/user/api';

const NicknamePage = () => {
  const navigate = useNavigate();
  const [nickname, setNickname] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // TODO: 실제로는 로그인된 사용자 ID를 가져와야 함
  const userId = 13; // 임시 하드코딩

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
    <div className="w-full h-full min-w-screen min-h-screen flex flex-col items-center justify-center bg-custom-black p-5 box-border absolute top-0 left-0 right-0 bottom-0">
      <div className="w-full max-w-[350px] bg-section-bg border border-primary rounded-xl p-4 flex flex-col gap-4 shadow-[0_4px_12px_rgba(0,230,255,0.1)]">
        {/* 헤더 */}
        <div className="text-center pb-2 border-b border-primary">
          <h1 className="text-subtitle text-custom-white mb-1">
            닉네임 입력
          </h1>
          <p className="text-desc text-custom-gray m-0">
            러닝 활동에 사용할 닉네임을 입력해주세요
          </p>
        </div>

        {/* 입력 폼 */}
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label className="block text-label text-custom-white mb-2">
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
              className={`w-full p-2.5 text-content border rounded-lg outline-none box-border transition-colors duration-200 ${
                error
                  ? 'border-accent-red bg-custom-black text-custom-white'
                  : isLoading
                  ? 'border-custom-gray bg-section-bg text-custom-gray opacity-50'
                  : 'border-custom-gray bg-custom-black text-custom-white focus:border-primary'
              }`}
            />
            {/* 글자수 표시 */}
            <div className="text-desc text-custom-gray text-right mt-1">
              {nickname.length}/10자
            </div>
          </div>

          {/* 에러 메시지 */}
          {error && (
            <div className="text-desc text-accent-red bg-[rgba(255,73,53,0.1)] p-2 rounded-lg border border-accent-red">
              ⚠ {error}
            </div>
          )}

          {/* 확인 버튼 */}
          <button
            type="submit"
            disabled={isLoading || !nickname.trim()}
            className={`w-full p-3 text-button rounded-lg transition-all duration-200 ${
              isLoading || !nickname.trim()
                ? 'text-custom-gray bg-section-bg border border-custom-gray cursor-not-allowed opacity-50'
                : 'text-custom-black bg-primary border border-primary cursor-pointer'
            }`}
          >
            {isLoading ? '처리 중...' : '확인'}
          </button>
        </form>

        {/* 안내 문구 */}
        <div className="text-desc text-custom-gray text-left pt-2 border-t border-dashed border-custom-gray leading-base">
          • 한글, 영문, 숫자만 사용 가능<br />
          • 2~10자 입력
        </div>
      </div>
    </div>
  );
};

export default NicknamePage;

