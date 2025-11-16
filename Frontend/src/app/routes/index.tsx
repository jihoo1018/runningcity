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
import CustomizePage from "@/pages/showroom/customize";
import ShowroomEditPage from "@/pages/showroom/edit";
import RunningPage from "@/pages/running";
import { ModalLayer } from "../modal/ModalLayer";
import FriendshipPage from "@/pages/friendship";
import AuthLandingPage from "@/pages/auth";
import LoginPage from "@/pages/login";
import SignupPage from "@/pages/signup";
import RequireAuth from "./guards/RequireAuth";
import RequireGuest from "./guards/RequireGuest";
import NotFoundPage from "@/pages/notFound";
import FriendRequestPage from "@/pages/friendship/request";

export function AppRoutes() {
  const location = useLocation();
  const state = location.state as { background?: Location } | undefined;
  const background = state?.background;

  return (
    <>
      <Routes location={background || location}>
        {/* 로그인이 필요한 메인 레이아웃 구역 */}
        <Route element={<RequireAuth><MainLayout /></RequireAuth>}>
        {/* <Route element={<MainLayout />}> */}
          <Route path="/" element={<HomePage />} />
          <Route path="/report" element={<RecordListPage />} />
          <Route path="/report/:sid" element={<ReportDetailPage />} />
          <Route path="/entry" element={<EntryPage />} />
          <Route path="/entry/:sid" element={<EntryResultPage />} />
          <Route path="/boutique" element={<BoutiquePage />} />
          <Route path="/showroom" element={<ShowroomPage />} />
          <Route path="/showroom/customize" element={<CustomizePage />} />
          <Route path="/showroom/edit" element={<ShowroomEditPage />} />
          <Route path="/friendship" element={<FriendshipPage />} />
          <Route path="/friendship/request" element={<FriendRequestPage />} />
          
          <Route path="*" element={<NotFoundPage />} />
        </Route>
        <Route path="/running" element={<RunningPage />} />
        <Route path="/m/*" element={<Navigate to="/" replace />} />

        <Route path="/nickname" element={<NicknamePage />} />
        <Route path="/onboarding" element={<OnboardingPage />} />

        {/* 인증 관련 페이지: 비로그인 사용자만 접근 가능 */}
        <Route
          path="/auth"
          element={
            <RequireGuest>
              <AuthLandingPage />
            </RequireGuest>
          }
        />
        <Route
          path="/login"
          element={
            <RequireGuest>
              <LoginPage />
            </RequireGuest>
          }
        />
        <Route
          path="/signup"
          element={
            <RequireGuest>
              <SignupPage />
            </RequireGuest>
          }
        />
      </Routes>

      {background && (
        <Routes>
          <Route path="/m/:flow/:step" element={<ModalLayer />} />
        </Routes>
      )}
    </>
  );
}
