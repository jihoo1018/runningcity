// src/app/providers/index.tsx
import { ReactNode } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

interface ProvidersProps {
  children: ReactNode;
}

const qc = new QueryClient();

export function Providers({ children }: ProvidersProps) {
  // 여기에 나중에 QueryClient, Zustand persist, Theme 등 추가
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}
