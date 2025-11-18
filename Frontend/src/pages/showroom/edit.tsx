import { useEffect, useState } from "react";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { BackIconButton } from "@/shared/ui/IconButtons";
import { PrivacySetting } from "@/entities/showroom/model/type";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import {
  fetchGetPrivacySetting,
  fetchSavePrivacySetting,
} from "@/entities/showroom/api/privacySetting";

// 프론트 metric key와 UI 라벨
const metricsList = [
  { key: "globalOpen", label: "글로벌 정보 공개 여부" },
  { key: "totalDist", label: "총 러닝" },
  { key: "maxDist", label: "최장 거리" },
  { key: "avgPace", label: "평균 페이스" },
  { key: "bestPace", label: "최고 기록(페이스)" },
  { key: "totalEntryCnt", label: "잠입 횟수" },
];

// 프론트 key → 백엔드 key 매핑
const frontendToBackendKey = {
  globalOpen: "globalPublic",
  totalDist: "showTotalRunning",
  maxDist: "showMaxDistance",
  avgPace: "showAvgPace",
  bestPace: "showBestPace",
  totalEntryCnt: "showHikingCount",
} as const;

// 백엔드 key → 프론트 key 매핑
const backendToFrontendKey = {
  globalPublic: "globalOpen",
  showTotalRunning: "totalDist",
  showMaxDistance: "maxDist",
  showAvgPace: "avgPace",
  showBestPace: "bestPace",
  showHikingCount: "totalEntryCnt",
} as const;

// 태그 목록
const tags = [
  "아침형",
  "저녁형",
  "새벽클럽",
  "주말러",
  "평일러",
  "출퇴근러",
  "이지런",
  "롱런",
  "인터벌",
  "혼자달려",
  "모아달려",
  "10K 완주목표",
  "하프도전",
  "풀코스도전",
  "풀코스러버",
];

const ShowroomEditPage = () => {
  const [selectedMetrics, setSelectedMetrics] = useState<string[]>([]);
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [loadedSetting, setLoadedSetting] = useState<PrivacySetting | null>(null);
  const { open } = useModalRouter();

  // -----------------------------
  // 🔵 1) 초기 데이터 로딩
  // -----------------------------
  useEffect(() => {
    fetchGetPrivacySetting()
      .then((data) => {
        // 1) 데이터 없으면 기본 전체 true
        if (!data) {
          const defaultMetrics = metricsList.map((m) => m.key);

          setSelectedMetrics(defaultMetrics);
          setSelectedTags([]);

          // 🔥 loadedSetting 도 기본값으로 설정해야 저장 시 에러 안남
          setLoadedSetting({
            userId: useAuthStore.getState().user?.userId ?? 0,
            globalPublic: true,
            showTotalRunning: true,
            showMaxDistance: true,
            showAvgPace: true,
            showBestPace: true,
            showHikingCount: true,
            tags: [],
          });

          return;
        }

        // 2) 데이터 있을 때
        setLoadedSetting(data);

        const metrics: string[] = [];

        if (data.globalPublic) metrics.push("globalOpen");
        if (data.showTotalRunning) metrics.push("totalDist");
        if (data.showMaxDistance) metrics.push("maxDist");
        if (data.showAvgPace) metrics.push("avgPace");
        if (data.showBestPace) metrics.push("bestPace");
        if (data.showHikingCount) metrics.push("totalEntryCnt");

        setSelectedMetrics(metrics);
        setSelectedTags(data.tags ?? []);
      })
      .catch(() => {
        // 3) 요청 실패해도 기본값 설정
        const defaultMetrics = metricsList.map((m) => m.key);

        setSelectedMetrics(defaultMetrics);
        setSelectedTags([]);

        setLoadedSetting({
          userId: useAuthStore.getState().user?.userId ?? 0,
          globalPublic: true,
          showTotalRunning: true,
          showMaxDistance: true,
          showAvgPace: true,
          showBestPace: true,
          showHikingCount: true,
          tags: [],
        });
      });
  }, []);

  // -----------------------------
  // 🔵 2) 체크박스 토글
  // -----------------------------
  const toggleMetric = (key: string) => {
    setSelectedMetrics((prev) =>
      prev.includes(key) ? prev.filter((m) => m !== key) : [...prev, key],
    );
  };

  // -----------------------------
  // 🔵 3) 태그 선택 (최대 4개)
  // -----------------------------
  const toggleTag = (tag: string) => {
    setSelectedTags((prev) => {
      if (prev.includes(tag)) {
        return prev.filter((t) => t !== tag);
      }
      if (prev.length >= 4) {
        open("privacySetting", "alert");
        return prev;
      }
      return [...prev, tag];
    });
  };

  // -----------------------------
  // 🔵 4) 저장 payload 구성
  // -----------------------------
  const buildPayload = (): PrivacySetting => {
    if (!loadedSetting) {
      throw new Error("초기 설정이 로드되지 않음");
    }

    return {
      userId: loadedSetting.userId,
      globalPublic: selectedMetrics.includes("globalOpen"),
      showTotalRunning: selectedMetrics.includes("totalDist"),
      showMaxDistance: selectedMetrics.includes("maxDist"),
      showAvgPace: selectedMetrics.includes("avgPace"),
      showBestPace: selectedMetrics.includes("bestPace"),
      showHikingCount: selectedMetrics.includes("totalEntryCnt"),
      tags: selectedTags,
    };
  };

  // -----------------------------
  // 🔵 5) 저장하기
  // -----------------------------
  const handleSave = async () => {
    const payload = buildPayload();
    await fetchSavePrivacySetting(payload);
    open("privacySetting", "confirm");
  };

  // -----------------------------
  // 🔵 UI
  // -----------------------------
  return (
    <div className="flex min-h-[calc(100vh-56px)] items-center justify-center px-4 pb-20">
      <div>
        {/* 제목 + 뒤로가기 */}
        <div className="relative mb-3 flex items-center justify-center">
          <div className="absolute left-0">
            <BackIconButton onClick={() => history.back()} />
          </div>

          <h1 className="text-xl font-bold text-cyan-300 drop-shadow-[0_0_6px_rgba(0,255,255,0.6)]">
            공개설정
          </h1>
        </div>

        {/* 내부 컨텐츠 */}
        <div className="flex flex-col gap-3">
          {/* 체크박스 영역 패널 */}
          <div className="rounded-xl border border-cyan-300 bg-[#0f1624]/60 p-4 shadow-[0_0_20px_rgba(0,255,255,0.05)]">
            <div className="flex flex-col gap-3">
              {metricsList.map((m) => (
                <label key={m.key} className="flex cursor-pointer items-center gap-2">
                  <input
                    type="checkbox"
                    checked={selectedMetrics.includes(m.key)}
                    onChange={() => toggleMetric(m.key)}
                    className="h-4 w-4 accent-cyan-300"
                  />
                  <span className="text-sm text-cyan-100">{m.label}</span>
                </label>
              ))}
            </div>
          </div>

          {/* 태그 영역 */}
          <div className="rounded-xl border border-cyan-300 bg-[#0f1624]/60 p-4 shadow-[0_0_20px_rgba(0,255,255,0.05)]">
            <p className="mb-3 rounded-lg border border-cyan-400/70 py-1 text-center text-sm text-cyan-200">
              태그 (최대 4개 선택)
            </p>

            <div className="flex flex-wrap gap-2">
              {tags.map((tag) => {
                const isSelected = selectedTags.includes(tag);
                return (
                  <button
                    key={tag}
                    onClick={() => toggleTag(tag)}
                    className={`rounded-full px-3 py-1 text-sm transition-all ${
                      isSelected
                        ? "bg-cyan-300 text-black shadow-[0_0_10px_rgba(0,255,255,0.6)]"
                        : "bg-section-bg border border-cyan-300 text-cyan-200"
                    } `}
                  >
                    {tag}
                  </button>
                );
              })}
            </div>
          </div>

          {/* 저장 버튼 */}
          <button
            onClick={handleSave}
            className="bg-primary mt-2 w-full rounded-xl from-cyan-400 to-cyan-600 py-3 text-center font-semibold text-black shadow-[0_0_15px_rgba(0,255,255,0.5)] transition-all hover:shadow-[0_0_25px_rgba(0,255,255,0.8)]"
          >
            저장하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default ShowroomEditPage;
