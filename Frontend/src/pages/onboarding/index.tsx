// src/pages/onboarding/index.tsx
import { useState, FormEvent, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { completeOnboarding } from "@/entities/user/api";
import type { FitnessLevel } from "@/entities/user";
import { AndroidBridge, initializeAndroidListener } from "@/shared/lib";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
const OnboardingPage = () => {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const [hasRunningHistory, setHasRunningHistory] = useState<boolean | null>(null);
  const [targetDistance, setTargetDistance] = useState("1");
  const [fitnessLevel, setFitnessLevel] = useState<FitnessLevel | "">("");
  const [restingHeartRate, setRestingHeartRate] = useState("");
  const [hasSmartWatch, setHasSmartWatch] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [isMeasuringHeartRate, setIsMeasuringHeartRate] = useState(false);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const measurementTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  useEffect(() => {
    if (!user) {
      navigate("/login", { replace: true });
    }
  }, [user, navigate]);
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    // 유효성 검사
    if (hasRunningHistory === null) {
      setError("러닝 이력을 선택해주세요");
      return;
    }
    if (!targetDistance || parseFloat(targetDistance) < 1 || parseFloat(targetDistance) > 40) {
      setError("목표 거리는 1~40km 사이로 입력해주세요");
      return;
    }
    if (!fitnessLevel) {
      setError("운동 능력치를 선택해주세요");
      return;
    }
    if (restingHeartRate && (parseInt(restingHeartRate) < 40 || parseInt(restingHeartRate) > 120)) {
      setError("심박수는 40~120 사이로 입력해주세요");
      return;
    }
    if (!user) {
      setError("Please log in again.");
      return;
    }
    setIsLoading(true);
    try {
      const onboardingData: any = {
        hasRunningHistory,
        fitnessLevel,
        targetDistanceKm: parseFloat(targetDistance),
        hasSmartWatch,
      };
      if (restingHeartRate) {
        onboardingData.restingHeartRate = parseInt(restingHeartRate);
      }
      const result = await completeOnboarding(user.userId, onboardingData);
      console.log("온보딩 완료:", result);
      // 성공 시 홈으로 이동
      navigate("/");
    } catch (err: any) {
      setIsLoading(false);
      // API 에러 응답 처리
      if (err.response?.data) {
        const errorData = err.response.data;
        // 이미 온보딩 완료 (409)
        if (errorData.code === "ONBOARDING_4090") {
          setError("이미 온보딩을 완료했습니다. 홈으로 이동합니다.");
          setTimeout(() => navigate("/"), 2000);
          return;
        }
        // 유효성 검증 실패 (400)
        if (errorData.code === "ONBOARDING_4000" && errorData.error?.details) {
          const fieldErrors = errorData.error.details
            .map((detail: any) => detail.message)
            .join(", ");
          setError(fieldErrors);
          return;
        }
        // 사용자 없음 (404)
        if (errorData.code === "USER_4040") {
          setError("사용자를 찾을 수 없습니다.");
          return;
        }
        // 기타 에러
        setError(errorData.message || "온보딩 중 오류가 발생했습니다.");
      } else {
        setError("네트워크 오류가 발생했습니다. 다시 시도해주세요.");
      }
      console.error("온보딩 실패:", err);
    }
  };
  // Android → React 메시지 수신 설정
  useEffect(() => {
    initializeAndroidListener((data) => {
      console.log("📩 Android 메시지 수신:", data);
      if (data.type === "HEART_RATE_MEASURED" && data.heartRate) {
        // 타임아웃 클리어
        if (measurementTimeoutRef.current) {
          clearTimeout(measurementTimeoutRef.current);
          measurementTimeoutRef.current = null;
        }
        setRestingHeartRate(data.heartRate.toString());
        setIsMeasuringHeartRate(false);
        setError("");
        console.log("✅ 심박수 측정 완료:", data.heartRate);
      } else if (data.type === "HEART_RATE_ERROR") {
        // 타임아웃 클리어
        if (measurementTimeoutRef.current) {
          clearTimeout(measurementTimeoutRef.current);
          measurementTimeoutRef.current = null;
        }
        console.error("❌ 심박수 측정 에러 수신:", data);
        setIsMeasuringHeartRate(false);
        const errorMsg = data.message || "심박수 측정에 실패했습니다";
        setError(errorMsg);
        console.error("❌ 에러 메시지:", errorMsg);
        // 측정 실패 시 restingHeartRate 초기화 (숫자가 넘어오지 않았으므로)
        if (!data.heartRate) {
          setRestingHeartRate("");
        }
      } else {
        console.log("📨 기타 메시지:", data);
      }
    });
  }, []);
  const handleMeasureHeartRate = () => {
    if (!hasSmartWatch) return;
    
    // 이전 타임아웃 클리어
    if (measurementTimeoutRef.current) {
      clearTimeout(measurementTimeoutRef.current);
      measurementTimeoutRef.current = null;
    }
    
    setIsMeasuringHeartRate(true);
    setError("");
    AndroidBridge.measureHeartRate();
    
    // 타임아웃 처리: 17초 후에도 응답이 없으면 버튼 초기화
    measurementTimeoutRef.current = setTimeout(() => {
      setIsMeasuringHeartRate((prev) => {
        if (prev) {
          console.warn("⚠️ 심박수 측정 타임아웃 - 버튼 초기화");
          setError("측정 시간이 초과되었습니다. 다시 시도해주세요.");
        }
        return false;
      });
      measurementTimeoutRef.current = null;
    }, 17000); // 17초 타임아웃
  };
  const fitnessOptions: { value: FitnessLevel; label: string }[] = [
    { value: "BEGINNER", label: "입문자 (처음 시작)" },
    { value: "INTERMEDIATE", label: "초급자 (가끔 운동)" },
    { value: "ADVANCED", label: "중급자 (주 2-3회)" },
    { value: "EXPERT", label: "상급자 (주 4-5회)" },
    { value: "ELITE", label: "전문가 (매일 운동)" },
  ];
  const selectedFitnessLabel = fitnessLevel
    ? fitnessOptions.find((opt) => opt.value === fitnessLevel)?.label || "선택해주세요"
    : "선택해주세요";
  // 외부 클릭/터치 시 드롭다운 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent | TouchEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
    };
    if (isDropdownOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      document.addEventListener("touchstart", handleClickOutside);
    }
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("touchstart", handleClickOutside);
    };
  }, [isDropdownOpen]);
  const handleSelectFitness = (value: FitnessLevel | "") => {
    setFitnessLevel(value);
    setIsDropdownOpen(false);
  };
  return (
    <div className="bg-custom-black absolute top-0 right-0 bottom-0 left-0 box-border flex h-full min-h-screen w-full min-w-screen flex-col items-center justify-center p-5">
      <div className="bg-section-bg border-primary flex w-full max-w-[350px] flex-col gap-4 rounded-xl border p-4 shadow-[0_4px_12px_rgba(0,230,255,0.1)]">
        {/* 헤더 */}
        <div className="border-primary border-b pb-2 text-center">
          <h1 className="text-subtitle text-custom-white mb-1">온보딩 정보 입력</h1>
          <p className="text-desc text-custom-gray m-0">러닝 활동에 필요한 정보를 입력해주세요</p>
        </div>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          {/* 러닝 이력 */}
          <div>
            <label className="text-label text-custom-white mb-2 block">러닝 이력</label>
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => setHasRunningHistory(true)}
                className={`text-content-bold flex-1 cursor-pointer rounded-lg p-2.5 transition-all duration-200 ${
                  hasRunningHistory === true
                    ? "text-custom-black bg-primary border-primary border"
                    : "text-custom-gray bg-section-bg border-custom-gray border"
                }`}
              >
                있음
              </button>
              <button
                type="button"
                onClick={() => setHasRunningHistory(false)}
                className={`text-content-bold flex-1 cursor-pointer rounded-lg p-2.5 transition-all duration-200 ${
                  hasRunningHistory === false
                    ? "text-custom-black bg-primary border-primary border"
                    : "text-custom-gray bg-section-bg border-custom-gray border"
                }`}
              >
                없음
              </button>
            </div>
          </div>
          {/* 목표 거리 */}
          <div className="flex flex-col gap-2">
            <div className="flex items-center justify-between px-1">
              <p className="text-label text-custom-gray">나의 목표 거리</p>
              <div className="flex items-baseline justify-center gap-1">
                <strong className="text-subtitle tabular-nums text-custom-white">{targetDistance || "1"}</strong>
                <span className="text-content text-custom-gray">km</span>
              </div>
            </div>
            <div
              className="p-[6px]"
              style={{
                border: "2px solid color-mix(in srgb, var(--color-primary), transparent 50%)",
              }}
            >
              <input
                type="range"
                name="targetDistanceKm"
                placeholder="1 ~ 40"
                min="1"
                max="40"
                step="1"
                value={targetDistance || "1"}
                onChange={(e) => {
                  const v = Math.min(40, Math.max(1, e.currentTarget.valueAsNumber));
                  const rounded = Math.round(v);
                  setTargetDistance(rounded.toString());
                }}
                style={
                  {
                    "--pct": `${((parseFloat(targetDistance || "1") - 1) / (40 - 1)) * 100}%`,
                  } as React.CSSProperties
                }
                className="rc-range block w-full appearance-none focus:outline-none"
                aria-label="목표 거리"
              />
            </div>
            <style>{`
              .rc-range {
                height: 14px;
                background:
                  /* 선택 구간 타일 */
                  repeating-linear-gradient(
                    90deg,
                    var(--color-primary) 0 10px,
                    transparent 10px 12px
                  ) left / var(--pct) 100% no-repeat,

                  /* 미선택 구간 타일 */
                  repeating-linear-gradient(
                    90deg,
                    color-mix(in srgb, var(--color-custom-gray), transparent 70%) 0 10px,
                    transparent 10px 12px
                  ) left / 100% 100% no-repeat,

                  /* 트랙 베이스 */
                  color-mix(in srgb, var(--color-custom-gray), transparent 80%);
              }

              /* WebKit */
              .rc-range::-webkit-slider-runnable-track {
                height: 18px;
                background: transparent;
                border-radius: 4px;
              }
              .rc-range::-webkit-slider-thumb {
                -webkit-appearance: none;
                height: 18px;
                width: 12px;
                margin-top: 0;
                border-radius: 2px;
                background: var(--color-primary);
                border: 1px solid color-mix(in srgb, var(--color-primary), black 20%);
                box-shadow:
                  0 0 0 2px color-mix(in srgb, var(--color-primary), transparent 60%),
                  0 0 0 4px color-mix(in srgb, var(--color-primary), transparent 85%); 
              }

              /* Firefox */
              .rc-range::-moz-range-track {
                height: 18px;
                background: transparent;
                border: none;
              }
              .rc-range::-moz-range-progress {
                height: 18px;
                background: transparent; 
              }
              .rc-range::-moz-range-thumb {
                height: 18px;
                width: 12px;
                border-radius: 2px;
                background: var(--color-primary);
                border: 1px solid color-mix(in srgb, var(--color-primary), black 20%);
              }

              /* Edge/Chromium(레거시) */
              .rc-range::-ms-fill-lower { background: transparent; }
              .rc-range::-ms-fill-upper { background: transparent; }
            `}</style>
          </div>
          {/* 운동 능력치 */}
          <div className="relative" ref={dropdownRef}>
            <label className="text-label text-custom-white mb-2 block">현재 운동 능력치</label>
            <button
              type="button"
              onClick={() => setIsDropdownOpen(!isDropdownOpen)}
              className={`text-content box-border flex w-full cursor-pointer items-center justify-between rounded-lg border p-2.5 text-left transition-colors duration-200 outline-none ${
                isDropdownOpen
                  ? "border-primary bg-custom-black text-custom-white"
                  : "border-custom-gray bg-custom-black text-custom-white focus:border-primary"
              }`}
            >
              <span className={fitnessLevel ? "text-custom-white" : "text-custom-gray"}>
                {selectedFitnessLabel}
              </span>
              <svg
                className={`h-4 w-4 transition-transform duration-200 ${isDropdownOpen ? "rotate-180" : ""}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M19 9l-7 7-7-7"
                />
              </svg>
            </button>
            {/* 드롭다운 옵션 리스트 */}
            {isDropdownOpen && (
              <div className="bg-custom-black border-primary absolute z-10 mt-1 w-full overflow-hidden rounded-lg border shadow-lg">
                <button
                  type="button"
                  onClick={() => handleSelectFitness("")}
                  className={`text-content w-full p-2.5 text-left transition-colors duration-200 ${
                    !fitnessLevel
                      ? "bg-section-bg text-custom-white"
                      : "text-custom-gray hover:bg-section-bg hover:text-custom-white"
                  }`}
                >
                  선택해주세요
                </button>
                {fitnessOptions.map((option) => (
                  <button
                    key={option.value}
                    type="button"
                    onClick={() => handleSelectFitness(option.value)}
                    className={`text-content border-custom-gray w-full border-t p-2.5 text-left transition-colors duration-200 ${
                      fitnessLevel === option.value
                        ? "bg-section-bg text-custom-white"
                        : "text-custom-gray hover:bg-section-bg hover:text-custom-white"
                    }`}
                  >
                    {option.label}
                  </button>
                ))}
              </div>
            )}
          </div>
          {/* 심박수 측정 */}
          <div>
            <label className="text-label text-custom-white mb-2 block">
              안정 심박수 (선택사항)
            </label>
            <div className="flex gap-2">
              <input
                type="number"
                value={restingHeartRate}
                onChange={(e) => setRestingHeartRate(e.target.value)}
                placeholder="40~120 bpm"
                min="40"
                max="120"
                disabled={!hasSmartWatch}
                className={`text-content border-custom-gray focus:border-primary box-border flex-1 rounded-lg border p-2.5 transition-colors duration-200 outline-none ${
                  !hasSmartWatch
                    ? "bg-section-bg text-custom-gray opacity-50"
                    : "bg-custom-black text-custom-white"
                }`}
              />
              <button
                type="button"
                disabled={!hasSmartWatch || isMeasuringHeartRate}
                onClick={handleMeasureHeartRate}
                className={`text-content-bold rounded-lg p-2.5 px-3.5 whitespace-nowrap transition-all duration-200 ${
                  !hasSmartWatch || isMeasuringHeartRate
                    ? "text-custom-gray bg-section-bg border-custom-gray cursor-not-allowed border opacity-50"
                    : "text-custom-black bg-primary border-primary cursor-pointer border"
                }`}
              >
                {isMeasuringHeartRate ? "측정 중..." : "측정"}
              </button>
            </div>
          </div>
          {/* 워치 체크박스 */}
          <div className="flex items-center justify-center p-1.5">
            <label className="text-desc text-custom-gray flex cursor-pointer items-center gap-2">
              <input
                type="checkbox"
                checked={!hasSmartWatch}
                onChange={(e) => {
                  setHasSmartWatch(!e.target.checked);
                  if (e.target.checked) {
                    setRestingHeartRate("");
                  }
                }}
                className="accent-primary h-[18px] w-[18px] cursor-pointer"
              />
              스마트워치가 없어요
            </label>
          </div>
          {/* 에러 메시지 */}
          {error && (
            <div className="text-desc text-accent-red border-accent-red rounded-lg border bg-[rgba(255,73,53,0.1)] p-2">
              ⚠ {error}
            </div>
          )}
          {/* 제출 버튼 */}
          <button
            type="submit"
            disabled={isLoading}
            className={`text-button w-full rounded-lg p-3 transition-all duration-200 ${
              isLoading
                ? "text-custom-gray bg-section-bg border-custom-gray cursor-not-allowed border opacity-50"
                : "text-custom-black bg-primary border-primary cursor-pointer border"
            }`}
          >
            {isLoading ? "제출 중..." : "제출하기"}
          </button>
        </form>
        {/* 안내 문구 */}
        <div className="text-desc text-custom-gray border-custom-gray leading-base border-t border-dashed pt-2 text-left">
          • 모든 정보는 맞춤형 러닝 추천에 사용됩니다
          <br />• 심박수는 스마트워치가 있을 때만 측정 가능합니다
        </div>
      </div>
    </div>
  );
};
export default OnboardingPage;
