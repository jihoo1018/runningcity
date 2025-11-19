// src/pages/running/index.tsx

import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { RunningMetrics } from "@/entities/run/ui/RunningMetrics";
import { RunningMetrics as RunningMetricsType, RunningState } from "@/entities/run/model/types";
import { AndroidBridge } from "@/shared/lib";

const RunningPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const routeState = location.state as { sessionId?: number; mode?: string } | null;

  const [state, setState] = useState<RunningState>("RUNNING");
  const [metrics, setMetrics] = useState<RunningMetricsType>({
    heartRate: 0, // 초기값 (더미 데이터)
    pace: 0, // 초기값 (더미 데이터)
    time: 0, // 초기값: 0초
    distance: 0.0, // 초기값 (더미 데이터)
  });
  const [progress, setProgress] = useState(50); // 진행률 (0-100)

  // 시간 업데이트 (러닝 중일 때만)
  useEffect(() => {
    if (state !== "RUNNING") return;

    const interval = setInterval(() => {
      setMetrics((prev) => ({
        ...prev,
        time: prev.time + 1,
      }));
    }, 1000);

    return () => clearInterval(interval);
  }, [state]);

  // 진행률 계산 (임시 - 목표 거리 대비)
  useEffect(() => {
    const targetDistance = 5; // 목표 거리 (km)
    const currentProgress = Math.min((metrics.distance / targetDistance) * 100, 100);
    setProgress(currentProgress);
  }, [metrics.distance]);

  const handlePause = () => {
    setState("PAUSED");
    // 워치에 일시정지 메시지 전송
    AndroidBridge.pauseRunning();
  };

  const handleResume = () => {
    setState("RUNNING");
    // 워치에 재개 메시지 전송
    AndroidBridge.resumeRunning();
  };

  const handleStop = () => {
    // 워치에 종료 메시지 전송
    AndroidBridge.stopRunning();
    // 결과 페이지로 이동
    navigate("/entry/result");
    // navigate("/entry/1");
  };

  return (
    <div className="bg-custom-black relative box-border flex h-full min-h-[calc(100vh-80px)] w-full flex-col items-center justify-center overflow-hidden p-5">
      {/* 사이버네틱 배경 */}
      <div className="pointer-events-none absolute inset-0">
        {/* 움직이는 원형 파티클 */}
        <div
          className="bg-primary absolute top-1/4 left-1/4 h-4 w-4 animate-pulse rounded-full opacity-30"
          style={{ animation: "float1 8s ease-in-out infinite" }}
        />
        <div
          className="bg-primary absolute top-1/3 right-1/4 h-3 w-3 animate-pulse rounded-full opacity-40"
          style={{ animation: "float2 10s ease-in-out infinite" }}
        />
        <div
          className="bg-primary absolute bottom-1/4 left-1/3 h-2.5 w-2.5 animate-pulse rounded-full opacity-50"
          style={{ animation: "float3 7s ease-in-out infinite" }}
        />
        <div
          className="bg-primary absolute right-1/3 bottom-1/3 h-5 w-5 animate-pulse rounded-full opacity-25"
          style={{ animation: "float4 9s ease-in-out infinite" }}
        />

        {/* 스캔 라인 효과 */}
        <div
          className="absolute inset-0 opacity-5"
          style={{
            background:
              "linear-gradient(180deg, transparent 0%, rgba(0, 230, 255, 0.3) 50%, transparent 100%)",
            animation: "scanLine 3s linear infinite",
          }}
        />
      </div>

      {/* CSS 애니메이션 */}
      <style>{`
        @keyframes float1 {
          0%, 100% { transform: translate(0, 0) scale(1); }
          25% { transform: translate(30px, -30px) scale(1.2); }
          50% { transform: translate(-20px, 20px) scale(0.8); }
          75% { transform: translate(20px, 30px) scale(1.1); }
        }
        
        @keyframes float2 {
          0%, 100% { transform: translate(0, 0) scale(1); }
          33% { transform: translate(-40px, 20px) scale(1.3); }
          66% { transform: translate(30px, -20px) scale(0.9); }
        }
        
        @keyframes float3 {
          0%, 100% { transform: translate(0, 0) scale(1); }
          50% { transform: translate(25px, -25px) scale(1.4); }
        }
        
        @keyframes float4 {
          0%, 100% { transform: translate(0, 0) scale(1); }
          25% { transform: translate(-30px, 40px) scale(0.7); }
          50% { transform: translate(40px, -10px) scale(1.5); }
          75% { transform: translate(-20px, -30px) scale(1.1); }
        }
        
        @keyframes scanLine {
          0% { transform: translateY(-100%); }
          100% { transform: translateY(100vh); }
        }
      `}</style>

      <div className="relative z-10 flex w-full max-w-[400px] flex-col gap-40">
        {/* 헤더 */}
        <div className="text-center">
          <h1 className="text-subtitle text-custom-white mb-3">
            {routeState?.mode === 'entry' ? '개인 잠입' : '에너지 모으기'}
          </h1>

          {/* 진행 바 */}
          <div className="bg-section-bg border-custom-gray h-2 w-full overflow-hidden rounded-full border">
            <div
              className="bg-primary h-full transition-all duration-300"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>

        {/* 메트릭 표시 */}
        <RunningMetrics metrics={metrics} />

        {/* 버튼 영역 */}
        <div className="flex justify-center gap-4">
          {state === "RUNNING" ? (
            // 러닝 중: 일시정지 버튼
            <button
              onClick={handlePause}
              className="bg-section-bg border-primary hover:bg-primary/20 flex h-24 w-24 cursor-pointer items-center justify-center rounded-full border transition-all duration-200 active:scale-95"
            >
              <p className="text-content text-custom-white">일시정지</p>
            </button>
          ) : state === "PAUSED" ? (
            // 일시정지 중: 종료, 재개 버튼
            <>
              <button
                onClick={handleStop}
                className="bg-section-bg border-accent-red hover:bg-accent-red/20 flex h-24 w-24 cursor-pointer items-center justify-center rounded-full border transition-all duration-200 active:scale-95"
              >
                <p className="text-content text-custom-white">종료</p>
              </button>
              <button
                onClick={handleResume}
                className="bg-section-bg border-primary hover:bg-primary/20 flex h-24 w-24 cursor-pointer items-center justify-center rounded-full border transition-all duration-200 active:scale-95"
              >
                <p className="text-content text-custom-white">재개</p>
              </button>
            </>
          ) : null}
        </div>
      </div>
    </div>
  );
};

export default RunningPage;
