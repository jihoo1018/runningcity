// src/shared/libs/kakao/kakaoMapsProvider.tsx
import { ReactNode, useEffect, useState } from "react";
import { loadKakaoMapsSDK } from "./loadKakaoMapsSDK";

export function KakaoMapsProvider({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let active = true;
    // SSR 안전 가드
    if (typeof window === "undefined") return;

    loadKakaoMapsSDK()
      .then(() => active && setReady(true))
      .catch((e) => {
        console.error("[KakaoMaps] load failed", e);
        // 실패해도 앱 전체가 죽지 않도록 처리
      });

    return () => { active = false; };
  }, []);

  return <>{children}</>;
}
