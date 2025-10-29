// src/app/routes/index.tsx
import { Routes, Route } from 'react-router-dom';
import HomePage from '../../pages/home';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
    </Routes>
  );
}
