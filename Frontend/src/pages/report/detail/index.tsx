// src/pages/report/detail/index.tsx

import { Suspense, lazy } from "react";
const ReportDetailPage = lazy(() => import("././ReportDetailPage"));

export default function ReportDetailRoute() {
  return (
    <Suspense fallback={<div className="p-4">불러오는 중…</div>}>
      <ReportDetailPage />
    </Suspense>
  );
}
