import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getEntryResult, completeEntrySession } from "@/entities/entry/api";
import KakaoRunningPreviewMap from "@/entities/entry/ui/KakaoRunningPreviewMap";
import { RunningSession, RewardType, Reward, GpsPoint } from "@/entities/entry/model/types";
import { metersToKm, formatPace, formatDuration } from "@/shared/lib/format";

// 🎲 리워드 확률표
const REWARD_TABLE = [
  { type: "EXP_SMALL", prob: 35, expRange: [50, 80], message: "가장 흔함" },
  { type: "EXP_MEDIUM", prob: 20, expRange: [120, 180], message: "괜찮음" },
  { type: "EXP_LARGE", prob: 5, expRange: [300, 400], message: "대박!" },
  { type: "CR_SMALL", prob: 25, crRange: [5, 10], message: "흔함" },
  { type: "CR_MEDIUM", prob: 10, crRange: [15, 25], message: "쏠쏠함" },
  { type: "CR_LARGE", prob: 3, crRange: [40, 60], message: "대박!" },
  {
    type: "EXP_CR",
    prob: 2,
    expRange: [150, 200],
    crRange: [15, 20],
    message: "잭팟!!",
  },
];

function getRandom(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function drawReward(): Reward {
  const total = REWARD_TABLE.reduce((acc, r) => acc + r.prob, 0);
  const rand = Math.random() * total;
  let cumulative = 0;
  for (const r of REWARD_TABLE) {
    cumulative += r.prob;
    if (rand <= cumulative) {
      return {
        type: r.type as RewardType,
        name: r.type.replace("_", " "),
        exp: r.expRange ? getRandom(r.expRange[0], r.expRange[1]) : 0,
        cr: r.crRange ? getRandom(r.crRange[0], r.crRange[1]) : 0,
        message: r.message,
      };
    }
  }
  return {
    type: "EXP_SMALL",
    name: "기본",
    exp: 50,
    cr: 0,
    message: "Default",
  };
}

function getRewards(cnt: number) {
  let exp = 0;
  switch (
    cnt // 잠입 인원수
  ) {
    case 2:
      exp = 1100;
      break;
    case 3:
      exp = 1300;
      break;
    case 4:
      exp = 1500;
      break;
    default:
      exp = 800;
  }
  return {
    exp: exp,
    credit: Math.floor(exp / 20),
  };
}

const EntryResultPage = () => {
  const navigate = useNavigate();
  const [reward, setReward] = useState<Reward | null>(null);
  const [resultData, setResultData] = useState<RunningSession | null>(null);
  const [bonusExp, setBonusExp] = useState(0); // 🔹 추가로 얻은 EXP
  const [bonusCr, setBonusCr] = useState(0); // 🔹 추가로 얻은 CR
  const [chipCount, setChipCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false); // ⚡ 요청 실패 시 상태 저장
  const { sid } = useParams();
  const sessionId = Number(sid); // 세션아이디값

  // ✅ 세션 데이터 불러오기
  useEffect(() => {
    async function fetchSession() {
      const data = await getEntryResult(sessionId);
      const rewards = getRewards(1); // TODO 나중에 개인/팀 잠입 나눠서 줘야함(1~4명)
      data.rewards = rewards;
      setResultData(data);
      setChipCount(data.dataChipCnt ?? 5);
    }
    fetchSession();
  }, []);

  // ✅ 리워드 열기 버튼 클릭
  const handleDraw = () => {
    if (!resultData || chipCount <= 0) return;

    const newReward = drawReward(); // 랜덤 리워드(확률)
    setReward(newReward);
    setBonusExp((prev) => prev + (newReward.exp ?? 0));
    setBonusCr((prev) => prev + (newReward.cr ?? 0));
    setChipCount((prev) => prev - 1);
  };

  /** ✅ 저장 (확인 버튼) */
  const handleConfirm = async () => {
    if (!resultData?.sessionId) {
      alert("세션 ID가 없습니다.");
      return;
    }

    setLoading(true);
    setError(false);

    const updatedSession = {
      ...resultData,
      rewards: {
        exp: resultData && resultData.rewards ? resultData.rewards.exp + bonusExp : 0 + bonusExp,
        credit:
          resultData && resultData.rewards ? resultData.rewards.credit + bonusCr : 0 + bonusCr,
      },
    };

    console.log("📦 서버로 보낼 데이터:", updatedSession);

    try {
      const success = await completeEntrySession(updatedSession);

      if (success) {
        alert("✅ 잠입 세션이 성공적으로 종료되었습니다.");
        navigate("/entry");
      } else {
        throw new Error("서버 응답 실패");
      }
    } catch (err) {
      console.error("❌ 서버 요청 실패:", err);
      alert("❌ 서버 요청 중 문제가 발생했습니다. 다시 시도해주세요.");
      setError(true); // 요청 실패 시 복구 플래그
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-[#0A1A2F] p-6 text-white">
      <h1 className="mb-6 text-2xl font-bold">📊 결과 리포트</h1>

      <div className="mb-6 w-full max-w-md rounded-2xl border border-cyan-300 bg-[#1B3240] p-4">
        {/* 지도 자리 */}
        <div className="mt-4">
          <KakaoRunningPreviewMap points={resultData?.gpsPoints ?? []}></KakaoRunningPreviewMap>
        </div>

        {/* 요약 정보 */}
        {resultData && resultData.summary ? (
          <div className="mt-4 mb-4 grid grid-cols-3 gap-3 pr-3 pl-3 text-sm">
            <div>
              <div className="text-gray-500">평균 페이스</div>
              <div className="font-medium">{formatPace(resultData.summary.avgPace)}</div>
            </div>
            <div>
              <div className="text-gray-500">시간</div>
              <div className="font-medium">{formatDuration(resultData.summary.duration)}</div>
            </div>
            <div>
              <div className="text-gray-500">칼로리</div>
              <div className="font-medium">{resultData.summary.totalCalories ?? "-"}</div>
            </div>
            <div>
              <div className="text-gray-500">고도 상승</div>
              <div className="font-medium">{resultData.summary.elevation ?? 0}</div>
            </div>
            <div>
              <div className="text-gray-500">평균 심박</div>
              <div className="font-medium">{resultData.summary.avgHeartRate ?? 0}</div>
            </div>
            <div>
              <div className="text-gray-500">케이던스</div>
              <div className="font-medium">{resultData.summary.avgCadence ?? 0}</div>
            </div>
          </div>
        ) : (
          // <p className="mb-4 text-center text-gray-300">
          //   🏃 거리: {(resultData.summary.totalDistance / 1000).toFixed(2)} km
          //   <br />
          //   ❤️ 평균심박수: {resultData.summary.avgHeartRate} bpm
          //   <br />
          //   🔥 칼로리: {resultData.summary.totalCalories} kcal
          //   <br />
          //   시간: {(resultData.summary.duration / 60).toFixed(1)}분
          // </p>
          <p className="mb-4 text-center text-gray-400">데이터 불러오는 중...</p>
        )}

        {/* 보상 영역 */}
        <div className="rounded-lg bg-[#13242F] p-4 text-center">
          <h2 className="mb-2 text-xl font-semibold">획득 보상 🎁</h2>
          <div className="mt-4 mb-4 grid grid-cols-2 gap-3 border-t pt-3 pr-3 pl-3 text-sm">
            {/* 🔹 EXP, CR = 기본값 + 추가 리워드 */}
            <div>
              <div className="text-gray-500">누적 경험치</div>
              <div className="font-medium">
                {resultData && resultData.rewards ? resultData.rewards.exp + bonusExp : 0} EXP
              </div>
            </div>
            <div>
              <div className="text-gray-500">누적 크레딧</div>
              <div className="font-medium">
                {resultData && resultData.rewards ? resultData.rewards.credit + bonusCr : 0} CR
              </div>
            </div>
          </div>

          <div className="mt-2 text-gray-300">
            <p className="mt-2 text-cyan-400">남은 데이터칩: {chipCount} 개</p>

            {reward ? (
              <div className="flex flex-col items-center gap-1">
                {/* EXP 표시 */}
                {Number(reward.exp) > 0 && (
                  <p className="font-semibold text-cyan-300">+{reward.exp} EXP</p>
                )}

                {/* CR 표시 */}
                {Number(reward.cr) > 0 && (
                  <p className="font-semibold text-yellow-300">+{reward.cr} CR</p>
                )}

                {/* EXP, CR 둘 다 0일 때만 표시 */}
                {!(Number(reward.exp) > 0) && !(Number(reward.cr) > 0) && (
                  <p className="text-sm text-gray-400"> ⚡ 꽝! 다음 칩을 열어보세요</p>
                )}
              </div>
            ) : (
              <p>데이터칩을 열어보세요 🔹</p>
            )}
          </div>
        </div>
      </div>

      {/* ✅ 칩 0개 시 저장 버튼 (재시도 가능) */}
      {chipCount > 0 ? (
        <button
          onClick={handleDraw}
          className="rounded-xl bg-cyan-500 px-6 py-2 font-semibold text-black transition-all hover:bg-cyan-400"
        >
          데이터칩 열기
        </button>
      ) : (
        <button
          onClick={handleConfirm}
          disabled={loading}
          className={`items-center justify-center rounded-xl px-6 py-2 font-semibold transition-all ${
            loading
              ? "bg-custom-gray text-custom-black cursor-wait"
              : error
                ? "bg-accent-red"
                : "bg-custom-white text-custom-black"
          }`}
        >
          {loading ? "저장 중..." : error ? "재시도" : "확인"}
        </button>
      )}
    </div>
  );
};

export default EntryResultPage;
