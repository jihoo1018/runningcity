// src/entities/report/ui/ReportRouteMap.tsx

import { useEffect, useRef } from "react";
import { loadKakaoMapsSDK } from "@/shared/lib/kakao/loadKakaoMapsSDK";

type Props = {
  geojson?: string | undefined;
  height?: number | undefined;
};
type LineString = { type: "LineString"; coordinates: number[][] };

export default function ReportRouteMap({ geojson, height = 260 }: Props) {
  const ref = useRef<HTMLDivElement>(null);

  // 지도 데이터가 없으면 간단한 대체 UI
  if (!geojson) {
    return (
      <div
        style={{ width: "100%", height }}
        className="border rounded flex items-center justify-center text-sm text-gray-500"
      >
        지도 데이터 없음
      </div>
    );
  }

  useEffect(() => {
    let canceled = false;
    let cleanup: (() => void) | undefined;

    (async () => {
      await loadKakaoMapsSDK();
      if (canceled || !ref.current) return;

      const kakao = (window as any).kakao;
      let parsed: LineString;
      try {
        parsed = JSON.parse(geojson) as LineString;
        if (parsed.type !== "LineString") throw new Error();
      } catch {
        console.warn("Invalid GeoJSON");
        return;
      }

      const path = parsed.coordinates.map(([lon, lat]) => new kakao.maps.LatLng(lat, lon));
      if (!path.length) return;

      const map = new kakao.maps.Map(ref.current, { center: path[0], level: 6 });
      const poly = new kakao.maps.Polyline({ map, path, strokeWeight: 5 });

      const bounds = new kakao.maps.LatLngBounds();
      path.forEach((p: any) => bounds.extend(p));
      map.setBounds(bounds);

      cleanup = () => poly.setMap(null);
    })();

    return () => {
      canceled = true;
      cleanup?.();
    };
  }, [geojson]);

  return <div ref={ref} style={{ width: "100%", height }} />;
}
