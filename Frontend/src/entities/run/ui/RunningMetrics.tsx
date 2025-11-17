// src/entities/run/ui/RunningMetrics.tsx

import { RunningMetrics as RunningMetricsType } from '../model/types';

interface RunningMetricsProps {
  metrics: RunningMetricsType;
}

export const RunningMetrics = ({ metrics }: RunningMetricsProps) => {
  // 시간 포맷팅 (초 -> 분:초)
  const formatTime = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}분 ${remainingSeconds}초`;
  };

  // 페이스 포맷팅 (초/km -> 분:초/km)
  const formatPace = (secondsPerKm: number): string => {
    if (secondsPerKm <= 0) return '0:00';
    const minutes = Math.floor(secondsPerKm / 60);
    const seconds = Math.floor(secondsPerKm % 60);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  // 거리 포맷팅 (km)
  const formatDistance = (km: number): string => {
    return `${km.toFixed(1)}km`;
  };

  return (
    <div className="w-full bg-section-bg border border-primary rounded-xl p-6 flex flex-col gap-6 shadow-[0_4px_12px_rgba(0,230,255,0.1)]">
      {/* 메트릭 그리드 */}
      <div className="grid grid-cols-3 gap-4">
        {/* 심박수 */}
        <div className="flex flex-col items-center gap-2">
          <p className="text-label text-custom-gray">심박수</p>
          <p className="text-content-bold text-custom-white">{metrics.heartRate}</p>
        </div>

        {/* 페이스 */}
        <div className="flex flex-col items-center gap-2">
          <p className="text-label text-custom-gray">페이스</p>
          <p className="text-content-bold text-custom-white">{formatPace(metrics.pace)}</p>
        </div>

        {/* 시간 */}
        <div className="flex flex-col items-center gap-2">
          <p className="text-label text-custom-gray">시간</p>
          <p className="text-content-bold text-custom-white">{formatTime(metrics.time)}</p>
        </div>
      </div>

      {/* 거리 - 큰 글씨로 강조 */}
      <div className="flex flex-col items-center gap-2 pt-2 border-t border-custom-gray">
        <p className="text-title text-custom-white">{formatDistance(metrics.distance)}</p>
      </div>
    </div>
  );
};

