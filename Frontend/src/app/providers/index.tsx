// src/app/providers/index.tsx
import { ReactNode } from 'react';

interface ProvidersProps {
  children: ReactNode;
}

export function Providers({ children }: ProvidersProps) {
  // 여기에 나중에 QueryClient, Zustand persist, Theme 등 추가
  return <>{children}</>;
}
