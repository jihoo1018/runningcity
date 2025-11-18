console.log("✅ React App Loaded");

import { useEffect } from "react";
import { HashRouter } from "react-router-dom";
import { Providers } from "./providers";
import { AppRoutes } from "./routes";
import { AppErrorBoundary } from "@/shared/error/AppErrorBoundary";
import { initializeAndroidListener } from "@/shared/lib";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import { apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";

function App() {
  // 전역 Android 리스너 설정 (앱이 시작될 때 한 번만)
  // 워치 데이터 저장만 전역으로 처리 (어느 페이지에 있든 데이터 수신 가능)
  useEffect(() => {
    console.log("🔧 [App] 전역 리스너 등록 (WORKOUT_RESULT 처리)");
    
    // initializeAndroidListener가 cleanup 함수를 반환
    const cleanup = initializeAndroidListener(async (data) => {
      // 워치 데이터 수신 처리만 전역으로
      if (data.type === "WORKOUT_RESULT") {
        try {
          console.log("📩 [App] 워치 데이터 수신");
          const workoutData = typeof data.data === "string" ? JSON.parse(data.data) : data.data;
          const userId = useAuthStore.getState().user?.userId;
          
          if (!userId) {
            console.error("❌ userId가 없습니다. 로그인이 필요합니다.");
            return;
          }
          
          console.log("💾 워치 데이터를 백엔드에 저장 중...", {
            userId,
            clientSecretKey: workoutData.clientSecretKey,
          });
          
          const requestBody = {
            clientSecretKey: workoutData.clientSecretKey || "",
            startTime: workoutData.startTime || 0,
            endTime: workoutData.endTime || 0,
            summary: workoutData.summary || {},
            cadenceRecords: workoutData.cadenceRecords || [],
            heartRateRecords: workoutData.heartRateRecords || [],
            gpsPoints: workoutData.gpsPoints || [],
          };
          
          const response = await apiPost<ApiResponse<void>>(
            `/sessions/watch?userId=${userId}`,
            requestBody,
          );
          
          if (response.status === 200 && response.code === "COMMON_2000") {
            console.log("✅ 워치 데이터 저장 완료!");
          } else {
            throw new Error(response.message || "저장 실패");
          }
        } catch (e) {
          console.error("❌ 워치 데이터 저장 실패:", e);
        }
      }
    });
    
    // cleanup 함수 반환 (앱이 종료될 때만 실행됨)
    return cleanup;
  }, []); // 빈 배열: 앱이 시작될 때 한 번만 실행

  return (
    <Providers>
      <HashRouter>
        <AppErrorBoundary>
          <AppRoutes />
        </AppErrorBoundary>
      </HashRouter>
    </Providers>
  );
}

export default App;
