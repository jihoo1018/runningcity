// src/entities/report/ui/loadKakaoMapsSDK.ts
import { ENV } from "@/shared/config/env";
declare global { interface Window { kakao?: any } }

let kakaoLoadPromise: Promise<void> | null = null;

export function loadKakaoMapsSDK() {
  if (kakaoLoadPromise) return kakaoLoadPromise;

  kakaoLoadPromise = new Promise<void>((resolve, reject) => {
    if (window.kakao?.maps) { resolve(); return; }

    const existed = Array.from(document.scripts).find(s =>
      s.src.includes("dapi.kakao.com/v2/maps/sdk.js")
    );

    if (existed) {
      const wait = () => (window.kakao?.maps ? resolve() : setTimeout(wait, 50));
      wait();
      return;
    }

    const s = document.createElement("script");
    s.async = true;
    s.src = `https://dapi.kakao.com/v2/maps/sdk.js?autoload=false&appkey=${ENV.KAKAO_MAP_APP_KEY}`;
    s.onload = () => window.kakao.maps.load(() => resolve());
    s.onerror = () => reject(new Error("Failed to load Kakao Maps SDK"));
    document.head.appendChild(s);
  });

  return kakaoLoadPromise;
}
