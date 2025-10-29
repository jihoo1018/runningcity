// src/shared/lib/webview.ts

/**
 * Android WebView 인터페이스 타입 정의
 */
interface AndroidInterface {
    startRunning: () => void;
    stopRunning: () => void;
    getGPSData: () => string;
    getHeartRate: () => number;
    showToast: (message: string) => void;
    vibrate: (duration: number) => void;
    // 필요한 기능 추가
}

// Window 객체에 Android 인터페이스 타입 추가
declare global {
    interface Window {
        Android?: AndroidInterface;
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
        return typeof window !== 'undefined' && !!window.Android;
    },

    /**
     * 러닝 시작
     */
    startRunning(): void {
        if (this.isAndroid()) {
            window.Android!.startRunning();
        } else {
            console.log('[DEV] 러닝 시작 (브라우저 환경)');
        }
    },

    /**
     * 러닝 종료
     */
    stopRunning(): void {
        if (this.isAndroid()) {
            window.Android!.stopRunning();
        } else {
            console.log('[DEV] 러닝 종료 (브라우저 환경)');
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
        console.log('[DEV] GPS 데이터 요청');
        return { lat: 37.5665, lng: 126.9780 };
    },

    /**
     * 심박수 가져오기
     */
    getHeartRate(): number {
        if (this.isAndroid()) {
            return window.Android!.getHeartRate();
        }
        console.log('[DEV] 심박수 요청');
        return 75; // 더미 데이터
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
    }
};
