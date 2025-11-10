// src/pages/home/index.tsx

import { useState, useEffect } from "react";
import { Button } from "../../shared/ui";
import { AndroidBridge, initializeAndroidListener } from "../../shared/lib";
import { Navbar } from "../../widgets/navbar";
import { MissionModal } from "../../features/mission";

const HomePage = () => {
  const [gpsData, setGpsData] = useState<{ lat: number; lng: number } | null>(
    null
  );
  // ✅ 모달 열기/닫기 상태
  const [missionOpen, setMissionOpen] = useState(false);
  // 러닝 상태 (Android → React로 동기화)
  const [isRunning, setIsRunning] = useState(false);

  // 수령한 코인 보여주고 지갑 상태 추가
  // const [wallet, setWallet] = useState(0); // 지갑 돈 늘어나는거 디버깅용.
  const setWallet = useState(0)[1];
  const userId = 1; //임시 유저 id
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

  const handleSettings = () => {
    AndroidBridge.showToast("설정");
  };

  const handleFriends = () => {
    AndroidBridge.showToast("친구");
  };

  const handleNotifications = () => {
    AndroidBridge.showToast("알림");
  };

  const handleQuest = () => {
    //AndroidBridge.showToast('퀘스트');
    setMissionOpen(true);
  };

  return (
    <div
      style={{
        width: "100%",
        height: "100%",
        minWidth: "100vw",
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        backgroundColor: "#f3f4f6",
        overflow: "hidden", // WebView 스크롤 방지
        position: "absolute", // fixed 대신 absolute 사용 (WebView 호환성)
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        boxSizing: "border-box",
      }}
    >
      {/* 메인 콘텐츠 영역 - 전체 화면 사용 */}
      <div
        style={{
          flex: 1,
          position: "relative", // 오버레이를 위한 relative 포지션
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          overflow: "hidden",
        }}
      >
        {/* 우측 상단: 세로로 정렬된 버튼들 */}
        <div
          style={{
            position: "absolute",
            top: "20px",
            right: "20px",
            display: "flex",
            flexDirection: "column",
            gap: "12px",
            zIndex: 10,
          }}
        >
          <Button label="설정" onClick={handleSettings} variant="circle" />
          <Button label="친구" onClick={handleFriends} variant="circle" />
          <Button label="알림" onClick={handleNotifications} variant="circle" />
          <Button label="퀘스트" onClick={handleQuest} variant="circle" />
        </div>

        {/* 캐릭터 영역 - 화면 중앙에 크게 */}
        <div
          style={{
            width: "60%", // vw 대신 % 사용
            height: "70%", // vh 대신 % 사용
            maxWidth: "300px",
            maxHeight: "400px",
            minWidth: "200px",
            minHeight: "250px",
            backgroundColor: "#e5e7eb",
            borderRadius: "20px",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            boxShadow: "0 8px 16px rgba(0,0,0,0.1)",
            position: "relative", // 러닝 버튼 오버레이를 위해
          }}
        >
          <p
            style={{
              color: "#6b7280",
              fontSize: "clamp(16px, 4vw, 24px)",
              textAlign: "center",
              margin: 0,
              fontWeight: "500",
            }}
          >
            캐릭터
            <br />
            이미지
          </p>

          {/* 러닝 버튼 - 캐릭터 위에 오버레이, 상태에 따라 버튼 텍스트 및 색상 변경 */}
          <button
            onClick={handleRunningButton}
            style={{
              position: "absolute",
              bottom: "20px",
              left: "50%",
              transform: "translateX(-50%)",
              padding: "clamp(12px, 3vw, 16px) clamp(20px, 5vw, 28px)",
              fontSize: "clamp(11px, 3vw, 14px)",
              fontWeight: "bold",
              backgroundColor: isRunning ? "#ef4444" : "#3b82f6", // 🟥 종료 시 빨간색
              color: "white",
              border: "none",
              borderRadius: "25px",
              cursor: "pointer",
              minHeight: "44px",
              minWidth: "200px",
              maxWidth: "280px",
              width: "90%",
              boxShadow: isRunning
                ? "0 4px 12px rgba(239, 68, 68, 0.4)"
                : "0 4px 12px rgba(59, 130, 246, 0.4)",
              WebkitTapHighlightColor: "transparent",
              touchAction: "manipulation",
              userSelect: "none",
              zIndex: 5,
              whiteSpace: "nowrap",
              overflow: "hidden",
              textOverflow: "ellipsis",
            }}
          >
            {isRunning ? "러닝 종료하기" : "러닝 시작하기"}
          </button>
        </div>

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
      </div>

      {/* 하단 네비게이션 바 */}
      <Navbar activeTab="홈" />

      {/* ✅ 미션 모달 */}
      <MissionModal
        open={missionOpen}
        onClose={() => setMissionOpen(false)}
        onClaimed={(coins) => setWallet((w) => w + coins)}
        userId={userId}
      />
    </div>
  );
};

export default HomePage;
