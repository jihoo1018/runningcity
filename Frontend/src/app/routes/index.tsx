// src/app/routes/index.tsx
import { Routes, Route } from 'react-router-dom';
import HomePage from '../../pages/home';
import NicknamePage from '../../pages/onboarding';
import OnboardingFormPage from '../../pages/onboarding-form';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/nickname" element={<NicknamePage />} />
      <Route path="/onboarding" element={<OnboardingFormPage />} />
    </Routes>
  );
}
