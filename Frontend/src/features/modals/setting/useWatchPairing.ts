import { useState, useEffect, useRef } from "react";
import { AndroidBridge, initializeAndroidListener } from "@/shared/lib";

export function useWatchPairing() {
  const [isPairing, setIsPairing] = useState(false);
  const [pairingMessage, setPairingMessage] = useState("");
  const pairingTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Android → React 메시지 수신 설정
  useEffect(() => {
    const cleanup = initializeAndroidListener((data) => {
      console.log("📩 [WatchPairing] Android 메시지 수신:", data);
      if (data.type === "WATCH_PAIRED") {
        // 타임아웃 클리어
        if (pairingTimeoutRef.current) {
          clearTimeout(pairingTimeoutRef.current);
          pairingTimeoutRef.current = null;
        }
        setIsPairing(false);
        setPairingMessage("워치 연동이 완료되었습니다!");
        // 2초 후 메시지 초기화
        setTimeout(() => {
          setPairingMessage("");
        }, 2000);
      } else if (data.type === "WATCH_PAIRING_ERROR") {
        // 타임아웃 클리어
        if (pairingTimeoutRef.current) {
          clearTimeout(pairingTimeoutRef.current);
          pairingTimeoutRef.current = null;
        }
        setIsPairing(false);
        setPairingMessage(data.message || "워치 연동에 실패했습니다");
      }
    });
    
    return cleanup;
  }, []);

  const pairWatch = () => {
    if (!AndroidBridge.isAndroid()) {
      setPairingMessage("안드로이드 환경에서만 사용 가능합니다");
      return;
    }

    // 이전 타임아웃 클리어
    if (pairingTimeoutRef.current) {
      clearTimeout(pairingTimeoutRef.current);
      pairingTimeoutRef.current = null;
    }

    setIsPairing(true);
    setPairingMessage("워치에서 확인 버튼을 눌러주세요...");
    AndroidBridge.pairWatch();

    // 타임아웃 처리: 10초 후에도 응답이 없으면 초기화
    pairingTimeoutRef.current = setTimeout(() => {
      setIsPairing((prev) => {
        if (prev) {
          setPairingMessage("연동 시간이 초과되었습니다. 다시 시도해주세요.");
        }
        return false;
      });
      pairingTimeoutRef.current = null;
    }, 10000);
  };

  return {
    isPairing,
    pairingMessage,
    pairWatch,
  };
}

