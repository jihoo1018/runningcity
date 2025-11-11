// src/app/routes/index.tsx
import { Routes, Route } from "react-router-dom";
import HomePage from "../../pages/home";
import NicknamePage from "../../pages/nickname";
import OnboardingPage from "../../pages/onboarding";
import RecordListPage from "../../pages/report";
import ReportDetailPage from "../../pages/report/detail";
import EntryPage from "../../pages/entry";
import EntryResultPage from "../../pages/entry/result";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/nickname" element={<NicknamePage />} />
      <Route path="/onboarding" element={<OnboardingPage />} />
      <Route path="/report" element={<RecordListPage />} />
      <Route path="/report/:sid" element={<ReportDetailPage />} />
      <Route path="/entry" element={<EntryPage />} />
      <Route path="/entry/result" element={<EntryResultPage />} />
    </Routes>
  );
}
