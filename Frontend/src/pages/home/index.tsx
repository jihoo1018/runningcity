import { useState, useEffect } from "react";
import { AndroidBridge, initializeAndroidListener } from "../../shared/lib";
// import { MissionModal } from "../../features/mission";
import { FloatingMenu } from "./ui/FloatingMenu";
import { CommonLinkButton } from "@/shared/ui/CommonLinkButton";
import UserProfileHeader from "./ui/UserProfileHeader";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import { apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";

const HomePage = () => {
  const [gpsData, setGpsData] = useState<{ lat: number; lng: number } | null>(null);
  // ✅ 모달 열기/닫기 상태
  const [missionOpen, setMissionOpen] = useState(false);
  // 러닝 상태 (Android → React로 동기화)
  const [isRunning, setIsRunning] = useState(false);

  // 수령한 코인 보여주고 지갑 상태 추가
  // const [wallet, setWallet] = useState(0); // 지갑 돈 늘어나는거 디버깅용.
  const setWallet = useState(0)[1];

  // 워치 데이터를 백엔드 API로 저장하는 함수
  const saveWatchDataToBackend = async (workoutData: any, userId: number): Promise<void> => {
    try {
      const requestBody = {
        clientSecretKey: workoutData.clientSecretKey || "",
        startTime: workoutData.startTime || 0,
        endTime: workoutData.endTime || 0,
        summary: {
          totalSteps: workoutData.summary?.totalSteps || 0,
          totalDistance: workoutData.summary?.totalDistance || 0,
          totalCalories: workoutData.summary?.totalCalories || 0,
          avgHeartRate: workoutData.summary?.avgHeartRate || 0,
          duration: workoutData.summary?.duration || 0,
          avgCadence: workoutData.summary?.avgCadence || 0,
          avgPace: workoutData.summary?.avgPace || 0,
          elevation: workoutData.summary?.elevation || 0,
        },
        cadenceRecords: (workoutData.cadenceRecords || []).map((record: any) => ({
          seq: record.seq || 0,
          cadence: record.cadence || 0,
          createdAt: record.createdAt || 0,
        })),
        heartRateRecords: (workoutData.heartRateRecords || []).map((record: any) => ({
          seq: record.seq || 0,
          heartRate: record.heartRate || 0,
          createdAt: record.createdAt || 0,
        })),
        gpsPoints: (workoutData.gpsPoints || []).map((point: any) => ({
          seq: point.seq || 0,
          latitude: point.latitude || 0,
          longitude: point.longitude || 0,
          altitude: point.altitude || 0,
          speed: point.speed || 0,
          createdAt: point.createdAt || 0,
        })),
      };

      console.log("📤 [홈 페이지] 백엔드에 워치 데이터 저장 요청:", requestBody);
      console.log("📤 [홈 페이지] 요청 URL: /sessions/watch?userId=" + userId);

      const response = await apiPost<ApiResponse<void>>(
        `/sessions/watch?userId=${userId}`,
        requestBody,
      );

      console.log("📥 [홈 페이지] 백엔드 응답:", response);
      console.log("📥 [홈 페이지] 응답 status:", response.status);
      console.log("📥 [홈 페이지] 응답 code:", response.code);

      if (response.status === 200 && response.code === "COMMON_2000") {
        console.log("✅ [홈 페이지] 워치 데이터 백엔드 저장 성공");
      } else {
        console.error(
          "❌ [홈 페이지] 저장 실패 - status:",
          response.status,
          "code:",
          response.code,
        );
        throw new Error(response.message || "저장 실패");
      }
    } catch (error: any) {
      console.error("❌ [홈 페이지] 워치 데이터 백엔드 저장 실패:", error);
      console.error("   에러 메시지:", error?.message);
      if (error?.response) {
        console.error("   응답 상태:", error.response.status);
        console.error("   응답 데이터:", error.response.data);
      }
      throw error;
    }
  };
  useEffect(() => {
    if (AndroidBridge.isAndroid()) {
      console.log("Android WebView 환경");
    } else {
      console.log("브라우저 환경 (개발 모드)");
    }

    // Android → React 메시지 수신
    initializeAndroidListener((data) => {
      if (data.type === "RUNNING_STATE") {
        console.log("현재 러닝 상태:", data.state);

        // ⚙️ Kotlin 쪽에서 전달: { type: "RUNNING_STATE", state: "RUNNING" | "STOPPED" }
        setIsRunning(data.state === "RUNNING");

        // (선택) 러닝 시작 시 GPS 갱신 요청
        if (data.state === "RUNNING") {
          const gps = AndroidBridge.getGPSData();
          setGpsData(gps);
        }
      }

      // 워치 결과 데이터 처리 (앱이 켜졌을 때 비동기로 받은 데이터)
      if (data.type === "WORKOUT_RESULT") {
        console.log("📊 워치 결과 데이터 수신 (홈 페이지):", data);
        console.log("📊 워치 결과 데이터 (data 필드):", data.data);

        if (data.data) {
          try {
            const workoutData = typeof data.data === "string" ? JSON.parse(data.data) : data.data;
            console.log("📊 워치 결과 데이터 (파싱 후):", workoutData);

            // ✅ zustand에서 현재 userId 가져오기
            const currentUserId = useAuthStore.getState().user?.userId;

            if (currentUserId == null) {
              console.warn("⚠️ 로그인되지 않은 상태 - 저장 불가");
              return;
            }

            // ✅ 워치 데이터의 userId를 실제 userId로 교체
            workoutData.userId = currentUserId;
            console.log("✅ userId 교체 완료:", workoutData.userId);

            // ✅ 백엔드 API로 데이터 저장
            saveWatchDataToBackend(workoutData, currentUserId)
              .then(() => {
                console.log("✅ 워치 데이터 백엔드 저장 완료");
              })
              .catch((error) => {
                console.error("❌ 워치 데이터 백엔드 저장 실패:", error);
                // 저장 실패해도 계속 진행 (Android에서 이미 저장했을 수 있음)
              });
          } catch (e) {
            console.error("❌ 워치 결과 데이터 파싱 실패:", e);
            console.error("❌ 원본 데이터:", data.data);
          }
        }
      }
    });
  }, []);

  const handleRunningButton = () => {
    if (!isRunning) {
      AndroidBridge.startRunning();
      AndroidBridge.showToast("러닝을 시작합니다! 🏃");
      const gps = AndroidBridge.getGPSData();
      setGpsData(gps);
      //   setIsRunning(true);
    } else {
      AndroidBridge.stopRunning();
      AndroidBridge.showToast("러닝을 종료했습니다. 🏁");
      //   setIsRunning(false);
    }
  };

  return (
    <div className="flex h-full w-full flex-col gap-4">
      <UserProfileHeader />
      <FloatingMenu />
      <div className="flex flex-1 items-center justify-center">캐릭터 영역</div>
      <CommonLinkButton
        href="/"
        className="shadow-primary/20 text-primary mb-3 border-2 py-5 text-lg font-bold shadow-lg"
      >
        러닝 에너지 모으기 ▶
      </CommonLinkButton>

      {/* 디버그 정보 (개발용) - 좌측 하단 */}
      {gpsData && (
        <div
          style={{
            position: "absolute",
            bottom: "20px",
            left: "20px",
            fontSize: "clamp(10px, 2.5vw, 12px)",
            color: "#9ca3af",
            backgroundColor: "rgba(255,255,255,0.8)",
            padding: "8px 12px",
            borderRadius: "8px",
            backdropFilter: "blur(4px)",
          }}
        >
          <p style={{ margin: 0 }}>
            GPS: {gpsData.lat.toFixed(4)}, {gpsData.lng.toFixed(4)}
          </p>
        </div>
      )}

      {/* (선택) 지갑/디버그 */}
      {/* <div style={{ marginTop: 12, textAlign: 'center' }}>
                    <div style={{ fontSize: 12, color: '#334155' }}>지갑: {wallet} 코인</div>
                </div> */}

      {/* ✅ 미션 모달 */}
      {/* <MissionModal
        open={missionOpen}
        onClose={() => setMissionOpen(false)}
        onClaimed={(coins) => setWallet((w) => w + coins)}
        userId={userId}
      /> */}
    </div>
  );
};

export default HomePage;
