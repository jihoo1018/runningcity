// src/app/routes/index.tsx
import { Routes, Route } from 'react-router-dom';
import HomePage from '../../pages/home';
import NicknamePage from '../../pages/nickname';
import OnboardingPage from '../../pages/onboarding';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/nickname" element={<NicknamePage />} />
      <Route path="/onboarding" element={<OnboardingPage />} />
    </Routes>
  );
}
