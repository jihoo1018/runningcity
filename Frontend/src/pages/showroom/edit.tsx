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
    <div className="border-primary bg-section-bg text-content mx-auto flex max-w-[480px] flex-col items-center rounded-xl border p-6">
      <div className="items-left mb-3 flex justify-between">
        {/* 왼쪽 뒤로가기 버튼 */}
        <div className="absolute left-8">
          <BackIconButton onClick={() => history.back()} />
        </div>
        {/* 중앙 제목 */}
        <h1 className="text-subtitle text-custom-white mx-auto text-center">공개설정</h1>{" "}
      </div>

      <div className="w-full max-w-[360px] rounded-xl shadow-md">
        {/* 체크박스 목록 */}
        <div className="flex flex-col gap-3">
          {metricsList.map((m) => (
            <label key={m.key} className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={selectedMetrics.includes(m.key)}
                onChange={() => toggleMetric(m.key)}
                className="h-4 w-4"
              />
              <span className="text-sm">{m.label}</span>
            </label>
          ))}
        </div>

        {/* 구분선 */}
        <div className="my-3 border-t" />

        {/* 태그 */}
        <p className="mb-3 border p-1 text-center text-sm">태그 (최대 4개 선택)</p>

        <div className="flex flex-wrap gap-2">
          {tags.map((tag) => {
            const isSelected = selectedTags.includes(tag);
            return (
              <button
                key={tag}
                onClick={() => toggleTag(tag)}
                className={`rounded-full px-3 py-1 text-sm ${
                  isSelected
                    ? "bg-primary text-custom-black"
                    : "border-primary text-custom-white border"
                }`}
              >
                {tag}
              </button>
            );
          })}
        </div>
      </div>

      <button
        onClick={handleSave}
        className="border-primary text-button mt-4 w-[200px] rounded-lg border py-3 text-white disabled:cursor-not-allowed disabled:opacity-50"
      >
        저장하기
      </button>
    </div>
  );
};

export default ShowroomEditPage;
