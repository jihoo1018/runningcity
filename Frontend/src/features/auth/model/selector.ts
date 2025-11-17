import { useShallow } from "zustand/shallow";
import { useAuthStore } from "./useAuthStore";

export const useAuthUser = () => useAuthStore((s) => s.user);
export const useUserId = () =>
  useAuthStore(
    useShallow((state) => ({
      userId: state.user?.userId ?? 0,
    })),
  );
export const useUserProfile = () =>
  useAuthStore(
    useShallow((state) => ({
      nickname: state.user?.nickname ?? "",
      userId: state.user?.userId ?? 0,
      totalExp: state.user?.totalExp ?? 0,
    })),
  );
