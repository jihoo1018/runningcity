// src/app/routes/guards/RequireAuth.tsx
import { Navigate, useLocation } from "react-router-dom";
import { useAuthStore, useIsAuthenticated } from "@/features/auth/model/useAuthStore";

type Props = { children: React.ReactElement };

export default function RequireAuth({ children }: Props) {
  const authed = useIsAuthenticated();
  const user = useAuthStore((s) => s.user);
  const location = useLocation();

  if (!authed) return <Navigate to="/auth" replace state={{ from: location }} />;

  if (user && !user.nickname && location.pathname !== "/nickname") {
    return <Navigate to="/nickname" replace state={{ from: location }} />;
  }

  return children;
}
