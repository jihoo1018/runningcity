import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getEntryResult,
  RunningSession,
  completeEntrySession,
} from "@/shared/api/session";

type RewardType =
  | "EXP_SMALL"
  | "EXP_MEDIUM"
  | "EXP_LARGE"
  | "CR_SMALL"
  | "CR_MEDIUM"
  | "CR_LARGE"
  | "EXP_CR";

interface Reward {
  type: RewardType;
  name: string;
  exp?: number;
  cr?: number;
  message: string;
}

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

  // ✅ 세션 데이터 불러오기
  useEffect(() => {
    async function fetchSession() {
      const sessionId = 2; // TODO 테스트용이라 실제로는 실데이터 넣어야함
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
        exp:
          resultData && resultData.rewards
            ? resultData.rewards.exp + bonusExp
            : 0 + bonusExp,
        credit:
          resultData && resultData.rewards
            ? resultData.rewards.credit + bonusCr
            : 0 + bonusCr,
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
    <div className="flex flex-col items-center justify-center min-h-screen bg-[#0A1A2F] text-white p-6">
      <h1 className="text-2xl font-bold mb-6">📊 결과 리포트</h1>

      <div className="w-full max-w-md bg-[#1B3240] border border-cyan-300 rounded-2xl p-4 mb-6">
        {/* 지도 자리 */}
        <div className="h-40 bg-gray-700 flex items-center justify-center rounded-lg mb-4">
          <span className="text-lg text-gray-300">🗺 지도</span>
        </div>

        {/* 요약 정보 */}
        {resultData ? (
          <p className="text-gray-300 text-center mb-4">
            🏃 거리: {(resultData.summary.totalDistance / 1000).toFixed(2)} km
            <br />
            ❤️ 평균심박수: {resultData.summary.avgHeartRate} bpm
            <br />
            🔥 칼로리: {resultData.summary.totalCalories} kcal
            <br />
            시간: {(resultData.summary.duration / 60).toFixed(1)}분
          </p>
        ) : (
          <p className="text-gray-400 text-center mb-4">
            데이터 불러오는 중...
          </p>
        )}

        {/* 보상 영역 */}
        <div className="bg-[#13242F] rounded-lg p-4 text-center">
          <h2 className="text-xl font-semibold mb-2">획득 보상 🎁</h2>

          {/* 🔹 EXP, CR = 기본값 + 추가 리워드 */}
          <p>
            누적 경험치:{" "}
            {resultData && resultData.rewards
              ? resultData.rewards.exp + bonusExp
              : 0}{" "}
            EXP
          </p>
          <p>
            누적 크레딧:{" "}
            {resultData && resultData.rewards
              ? resultData.rewards.credit + bonusCr
              : 0}{" "}
            CR
          </p>
          <p className="mt-2 text-cyan-400">남은 데이터칩: {chipCount} 개</p>

          <div className="mt-2 text-gray-300">
            {reward ? (
              <div className="flex flex-col items-center gap-1">
                {/* EXP 표시 */}
                {Number(reward.exp) > 0 && (
                  <p className="text-cyan-300 font-semibold">
                    +{reward.exp} EXP
                  </p>
                )}

                {/* CR 표시 */}
                {Number(reward.cr) > 0 && (
                  <p className="text-yellow-300 font-semibold">
                    +{reward.cr} CR
                  </p>
                )}

                {/* EXP, CR 둘 다 0일 때만 표시 */}
                {!(Number(reward.exp) > 0) && !(Number(reward.cr) > 0) && (
                  <p className="text-gray-400 text-sm">
                    {" "}
                    ⚡ 꽝! 다음 칩을 열어보세요
                  </p>
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
          className="px-6 py-2 rounded-xl font-semibold bg-cyan-500 hover:bg-cyan-400 text-black transition-all"
        >
          데이터칩 열기
        </button>
      ) : (
        <button
          onClick={handleConfirm}
          disabled={loading}
          className={`px-6 py-2 rounded-xl font-semibold justify-center items-center transition-all ${
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
