import { useState, useEffect } from "react";
import { AndroidBridge, initializeAndroidListener } from "../../shared/lib";
import { FloatingMenu } from "./ui/FloatingMenu";
import { CommonLinkButton } from "@/shared/ui/CommonLinkButton";
import UserProfileHeader from "./ui/UserProfileHeader";

const HomePage = () => {
  const [gpsData, setGpsData] = useState<{ lat: number; lng: number } | null>(null);
  // 러닝 상태 (Android → React로 동기화)
  const [isRunning, setIsRunning] = useState(false);
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

    </div>
  );
};

export default HomePage;
