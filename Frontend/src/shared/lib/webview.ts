// src/shared/lib/webview.ts

/**
 * Android WebView 인터페이스 타입 정의
 */
interface AndroidInterface {
  startRunning: (sessionId: string) => void;
  stopRunning: (sessionId: string) => void;
  pauseRunning: (sessionId: string) => void;
  resumeRunning: (sessionId: string) => void;
  getGPSData: () => string;
  getHeartRate: () => number;
  measureHeartRate: () => void;
  pairWatch: () => void;
  showToast: (message: string) => void;
  vibrate: (duration: number) => void;
  // 필요한 기능 추가
}

// Window 객체에 Android 인터페이스 타입 추가
declare global {
  interface Window {
    Android?: AndroidInterface;
    onAndroidMessage?: (data: any) => void;
    _androidListeners?: Set<(data: any) => void>;
  }
}

/**
 * Android 네이티브 기능 호출 헬퍼
 */
export const AndroidBridge = {
  /**
   * Android 환경인지 확인
   */
  isAndroid(): boolean {
    return typeof window !== "undefined" && !!window.Android;
  },

  /**
   * 러닝 시작
   */
  startRunning(sessionId: string = "0"): void {
    if (this.isAndroid()) {
      window.Android!.startRunning(sessionId);
    } else {
      console.log("[DEV] 러닝 시작 (브라우저 환경)");
    }
  },

  /**
   * 러닝 종료
   */
  stopRunning(sessionId: string = "0"): void {
    if (this.isAndroid()) {
      window.Android!.stopRunning(sessionId);
    } else {
      console.log("[DEV] 러닝 종료 (브라우저 환경)");
    }
  },
  
  /**
   * 러닝 일시정지
   */
  pauseRunning(sessionId: string = "0"): void {
    if (this.isAndroid()) {
      window.Android!.pauseRunning(sessionId);
    } else {
      console.log("[DEV] 러닝 일시정지 (브라우저 환경)");
    }
  },
  
  /**
   * 러닝 재개
   */
  resumeRunning(sessionId: string = "0"): void {
    if (this.isAndroid()) {
      window.Android!.resumeRunning(sessionId);
    } else {
      console.log("[DEV] 러닝 재개 (브라우저 환경)");
    }
  },

  /**
   * GPS 데이터 가져오기
   */
  getGPSData(): { lat: number; lng: number } | null {
    if (this.isAndroid()) {
      const data = window.Android!.getGPSData();
      try {
        return JSON.parse(data);
      } catch {
        return null;
      }
    }
    // 개발 환경 더미 데이터
    console.log("[DEV] GPS 데이터 요청");
    return { lat: 37.5665, lng: 126.978 };
  },

  /**
   * 심박수 가져오기
   */
  getHeartRate(): number {
    if (this.isAndroid()) {
      return window.Android!.getHeartRate();
    }
    console.log("[DEV] 심박수 요청");
    return 75; // 더미 데이터
  },

  /**
   * 워치에서 심박수 측정 요청
   */
  measureHeartRate(): void {
    if (this.isAndroid()) {
      window.Android!.measureHeartRate();
    } else {
      console.log("[DEV] 심박수 측정 요청 (브라우저 환경)");
      // 개발 환경에서는 더미 데이터로 시뮬레이션
      setTimeout(() => {
        if (window.onAndroidMessage) {
          window.onAndroidMessage({
            type: "HEART_RATE_MEASURED",
            heartRate: 72,
          });
        }
      }, 2000);
    }
  },

  /**
   * 워치 연동 요청
   */
  pairWatch(): void {
    if (this.isAndroid()) {
      window.Android!.pairWatch();
    } else {
      console.log("[DEV] 워치 연동 요청 (브라우저 환경)");
      // 개발 환경에서는 더미 데이터로 시뮬레이션
      setTimeout(() => {
        if (window.onAndroidMessage) {
          window.onAndroidMessage({
            type: "WATCH_PAIRED",
          });
        }
      }, 2000);
    }
  },

  /**
   * 토스트 메시지 표시
   */
  showToast(message: string): void {
    if (this.isAndroid()) {
      window.Android!.showToast(message);
    } else {
      console.log(`[DEV] Toast: ${message}`);
      // 브라우저 환경에서는 alert로 대체
      // alert(message);
    }
  },

  /**
   * 진동
   */
  vibrate(duration: number = 100): void {
    if (this.isAndroid()) {
      window.Android!.vibrate(duration);
    } else {
      console.log(`[DEV] 진동: ${duration}ms`);
      // 브라우저 진동 API (지원하는 경우)
      if (navigator.vibrate) {
        navigator.vibrate(duration);
      }
    }
  },
};

/**
 * ✅ Android → React: 이벤트 수신 핸들러
 * Android 쪽에서 webView.evaluateJavascript("window.onAndroidMessage(...)") 호출 시 실행됨
 * 
 * 여러 컴포넌트에서 동시에 리스닝 가능 - Set으로 관리
 */
export function initializeAndroidListener(callback: (data: any) => void): () => void {
  // 리스너 Set 초기화 (처음 한 번만)
  if (!window._androidListeners) {
    window._androidListeners = new Set();
    
    // 전역 핸들러 설정 (처음 한 번만)
    window.onAndroidMessage = (data: any) => {
      console.log("📩 Received from Android:", data);
      // 등록된 모든 리스너에게 브로드캐스트
      window._androidListeners?.forEach((listener) => {
        try {
          listener(data);
        } catch (error) {
          console.error("❌ Listener error:", error);
        }
      });
    };
    
    console.log("🔧 Android 메시지 브로드캐스터 초기화 완료");
  }
  
  // 리스너 추가
  window._androidListeners.add(callback);
  console.log(`✅ 리스너 추가 (총 ${window._androidListeners.size}개 활성화)`);
  
  // cleanup 함수 반환 (useEffect의 return에서 사용)
  return () => {
    window._androidListeners?.delete(callback);
    console.log(`🧹 리스너 제거 (남은 개수: ${window._androidListeners?.size || 0}개)`);
  };
}