// src/pages/report/detail/ReportDetailPage.tsx

import { useMemo } from "react";
import { useParams } from "react-router-dom";
import { useReportDetailQuery } from "@/entities/report/api/useReportDetailQuery";
import ReportRouteMap from "@/entities/report/ui/ReportRouteMap";
import { metersToKm, formatPace, formatDuration } from "@/shared/lib/format";

export default function ReportDetailPage() {
  const { sid } = useParams();
  const sessionId = Number(sid);
  const { data, isLoading, isError } = useReportDetailQuery(sessionId);

  const s = data?.summary;

  const bigDistance = useMemo(
    () => (s?.totalDistance ? (s.totalDistance / 1000).toFixed(1) : "-"),
    [s?.totalDistance]
  );

  // ✅ AI Report: DB에 aiReport(줄글 텍스트)로 들어올 예정.
  //    - 지금은 백엔드 미연결 → 하드코딩 기본 문단 사용
  //    - 서버 연결되면 data.aiReport(문자열)를 그대로 표기
  const defaultAiParagraph =
    "이번 러닝은 페이스와 심박의 균형이 전반적으로 안정적이었습니다. " +
    "중반 이후 페이스 드롭이 적어 피로 누적 관리가 잘 된 편입니다. " +
    "다음 러닝에서는 워밍업(10분) 후 본훈련 구간에서 페이스를 3~5% 상향해보세요. " +
    "케이던스는 현재 리듬을 유지하되 보폭을 과하게 늘리지 않도록 주의하면 효율이 더 좋아집니다.";

  const aiText =
    typeof (data as any)?.aiReport === "string" && (data as any).aiReport.trim().length > 0
      ? (data as any).aiReport
      : defaultAiParagraph;

  if (isLoading) return <div className="p-4">불러오는 중…</div>;
  if (isError || !data) return <div className="p-4 text-red-600">기록을 불러오지 못했습니다.</div>;

  return (
    <div className="p-4 max-w-md mx-auto">
      <div className="text-sm text-gray-500 mb-1">기록 상세</div>

      <div className="flex items-end gap-2">
        <div className="text-4xl font-bold">
          {bigDistance}<span className="text-2xl">km</span>
        </div>
        <span className="px-2 py-1 text-xs rounded-full border">
          {data.type === "ENTRY" ? "기지 잠입" : "에너지"}
        </span>
      </div>

      <div className="mt-4 border-t pt-3 grid grid-cols-3 gap-3 text-sm">
        <div>
          <div className="text-gray-500">평균 페이스</div>
          <div className="font-medium">{formatPace(s?.avgPace)}</div>
        </div>
        <div>
          <div className="text-gray-500">시간</div>
          <div className="font-medium">{formatDuration(s?.duration)}</div>
        </div>
        <div>
          <div className="text-gray-500">칼로리</div>
          <div className="font-medium">{s?.totalCalories ?? "-"}</div>
        </div>
        <div>
          <div className="text-gray-500">고도 상승</div>
          <div className="font-medium">{s?.elevation ?? 0}</div>
        </div>
        <div>
          <div className="text-gray-500">평균 심박</div>
          <div className="font-medium">{s?.avgHeartRate ?? 0}</div>
        </div>
        <div>
          <div className="text-gray-500">케이던스</div>
          <div className="font-medium">{s?.avgCadence ?? 0}</div>
        </div>
      </div>

      <div className="mt-4">
        <ReportRouteMap geojson={data.route?.geojson} height={260} />
      </div>

      {/* AI Report: 줄글 그대로 렌더링 */}
      <div className="mt-6 p-4 rounded border">
        <div className="text-sm font-semibold mb-2">AI Report</div>
        <p className="text-sm leading-relaxed whitespace-pre-line">
          {aiText}
        </p>
      </div>

      <div className="mt-4">
        <div className="text-base font-semibold mb-2">획득 보상</div>
        <div className="grid grid-cols-2 gap-2">
          <div className="border rounded p-3">
            <div className="text-gray-500 text-sm">크레딧</div>
            <div className="text-lg font-bold">{data.rewards?.credit ?? 0}</div>
          </div>
          <div className="border rounded p-3">
            <div className="text-gray-500 text-sm">경험치</div>
            <div className="text-lg font-bold">{data.rewards?.exp ?? 0}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
