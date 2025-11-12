import { Routes, Route, useLocation, Navigate } from "react-router-dom";
import type { Location } from "react-router-dom";
import HomePage from "@/pages/home";
import NicknamePage from "@/pages/nickname";
import OnboardingPage from "@/pages/onboarding";
import RecordListPage from "@/pages/report";
import ReportDetailPage from "@/pages/report/detail";
import EntryPage from "@/pages/entry";
import EntryResultPage from "@/pages/entry/result";
import { MainLayout } from "../layout/MainLayout";
import BoutiquePage from "@/pages/boutique";
import ShowroomPage from "@/pages/showroom";
import { ModalLayer } from "../modal/ModalLayer";

export function AppRoutes() {
  const location = useLocation();
  const state = location.state as { background?: Location } | undefined;
  const background = state?.background;

  return (
    <>
      <Routes location={background || location}>
        <Route element={<MainLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/report" element={<RecordListPage />} />
          <Route path="/report/:sid" element={<ReportDetailPage />} />
          <Route path="/entry" element={<EntryPage />} />
          <Route path="/entry/result" element={<EntryResultPage />} />
          <Route path="/boutique" element={<BoutiquePage />} />
          <Route path="/showroom" element={<ShowroomPage />} />
        </Route>

        <Route path="/m/*" element={<Navigate to="/" replace />} />

        <Route path="/nickname" element={<NicknamePage />} />
        <Route path="/onboarding" element={<OnboardingPage />} />
      </Routes>

      {background && (
        <Routes>
          <Route path="/m/:flow/:step" element={<ModalLayer />} />
        </Routes>
      )}
    </>
  );
}
