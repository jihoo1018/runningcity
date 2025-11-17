// src/features/auth/model/useAuthStore.ts
import { create } from "zustand";
import { persist } from "zustand/middleware";

export type AuthUser = {
  userId: number;
  nickname?: string | null;
  userCode?: string;
  totalExp?: number;
  email?: string;
};

export type AuthState = {
  user: AuthUser | null;
  setUser: (user: AuthUser) => void;
  clear: () => void;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      setUser: (user) => set({ user }),
      clear: () => set({ user: null }),
    }),
    { name: "auth" },
  ),
);

export const useIsAuthenticated = () => !!useAuthStore((s) => s.user);
