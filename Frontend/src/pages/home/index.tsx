// src/pages/home/index.tsx

import { useState, useEffect, useMemo } from "react";
import { AndroidBridge, initializeAndroidListener } from "../../shared/lib";
import { FloatingMenu } from "./ui/FloatingMenu";
import { CommonLinkButton } from "@/shared/ui/CommonLinkButton";
import UserProfileHeader from "./ui/UserProfileHeader";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import { apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";
import { fetchGetEquippedItems } from "@/entities/showroom/api/customize";
import { LPCCharacterRenderer } from "@/entities/showroom/ui/LPCCharacterRenderer";
import { useAvatarStore } from "@/features/avatar/model/avatarStore";
import { slotsToArray } from "@/entities/showroom/model/slotUtils";

const HomePage = () => {
  const [gpsData, setGpsData] = useState<{ lat: number; lng: number } | null>(null);
  const [missionOpen, setMissionOpen] = useState(false);
  const [isRunning, setIsRunning] = useState(false);

  const setWallet = useState(0)[1];

  /** 워치 데이터 저장 */
  const saveWatchDataToBackend = async (workoutData: any, userId: number) => {
    try {
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

      if (!(response.status === 200 && response.code === "COMMON_2000")) {
        throw new Error(response.message || "저장 실패");
      }
    } catch (e) {
      console.error("워치 데이터 저장 실패:", e);
    }
  };

  /** Android Listener */
  useEffect(() => {
    initializeAndroidListener((data) => {
      if (data.type === "RUNNING_STATE") {
        setIsRunning(data.state === "RUNNING");

        if (data.state === "RUNNING") {
          const gps = AndroidBridge.getGPSData();
          setGpsData(gps);
        }
      }

      if (data.type === "WORKOUT_RESULT") {
        const workoutData = typeof data.data === "string" ? JSON.parse(data.data) : data.data;
        const userId = useAuthStore.getState().user?.userId;
        if (!userId) return;

        saveWatchDataToBackend(workoutData, userId);
      }
    });
  }, []);

  /** 러닝 시작/종료 */
  const handleRunningButton = () => {
    if (!isRunning) {
      AndroidBridge.startRunning();
      AndroidBridge.showToast("러닝을 시작합니다! 🏃");
      setGpsData(AndroidBridge.getGPSData());
    } else {
      AndroidBridge.stopRunning();
      AndroidBridge.showToast("러닝을 종료했습니다. 🏁");
    }
  };

  /** 캐릭터 로딩 */
  const slots = useAvatarStore((s) => s.slots);
  const sync = useAvatarStore((s) => s.syncFromServer);
  const equippedItems = useMemo(() => slotsToArray(slots), [slots]);

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchGetEquippedItems().then((data) => {
      sync(data);
      setLoading(false);
    });
  }, []);

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center text-white">
        캐릭터 불러오는 중...
      </div>
    );
  }

  return (
    <div className="flex h-full w-full flex-col gap-4">
      <UserProfileHeader />
      <FloatingMenu />
      ``
      {/* ----------------------------- */}
      {/* 캐릭터 + 포탈 영역 */}
      {/* ----------------------------- */}
      <div className="flex flex-1 items-end justify-center pb-6">
        <div className="relative flex items-center justify-center">
          <div className="relative z-10 mb-15 scale-200">
            <LPCCharacterRenderer items={equippedItems} direction={2} animation="walk" />
          </div>
        </div>
      </div>
      <CommonLinkButton
        href="/running"
        className="shadow-primary/20 text-primary mb-3 border-2 py-5 text-lg font-bold shadow-lg"
      >
        러닝 에너지 모으기 ▶
      </CommonLinkButton>
      {/* GPS Debug */}
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
    </div>
  );
};

export default HomePage;
