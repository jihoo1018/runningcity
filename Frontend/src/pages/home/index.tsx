import { useState, useEffect, useMemo } from "react";
import { AndroidBridge } from "../../shared/lib";
import { FloatingMenu } from "./ui/FloatingMenu";
import { CommonLinkButton } from "@/shared/ui/CommonLinkButton";
import UserProfileHeader from "./ui/UserProfileHeader";
import { fetchGetEquippedItems } from "@/entities/showroom/api/customize";
import { LPCCharacterRenderer } from "@/entities/showroom/ui/LPCCharacterRenderer";
import { useAvatarStore } from "@/features/avatar/model/avatarStore";
import { slotsToArray } from "@/entities/showroom/model/slotUtils";

const HomePage = () => {
  const [gpsData, setGpsData] = useState<{ lat: number; lng: number } | null>(null);
  const [missionOpen, setMissionOpen] = useState(false);
  const [isRunning, setIsRunning] = useState(false);

  const setWallet = useState(0)[1];

  /** Android 메시지 리스너 (페이지별 처리) */
  useEffect(() => {
    // 전역 리스너는 App.tsx에서 설정되므로 여기서는 제거
    // 필요한 경우 페이지별 상태 업데이트만 처리
    console.log("📱 HomePage 마운트됨");
    
    return () => {
      console.log("📱 HomePage 언마운트됨");
    };
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
      {/* ----------------------------- */}
      {/* 캐릭터 + 포탈 영역 */}
      {/* ----------------------------- */}
      <div className="flex flex-1 items-end justify-center pb-6">
        <div className="relative flex items-center justify-center">
          <div className="relative z-10 mb-10 scale-130">
            <LPCCharacterRenderer items={equippedItems} direction={2} animation="walk" />
          </div>
        </div>
      </div>
      <CommonLinkButton
        href="/running"
        className="shadow-primary/20 text-primary mb-8 border-2 py-5 text-lg font-bold shadow-lg"
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
