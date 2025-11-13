// src/app/routes/guards/RequireAuth.tsx
import { Navigate, useLocation } from "react-router-dom";
import { useIsAuthenticated } from "@/features/auth/model/useAuthStore";

type Props = { children: React.ReactElement };

export default function RequireAuth({ children }: Props) {
  const authed = useIsAuthenticated();
  const location = useLocation();
  if (!authed) return <Navigate to="/auth" replace state={{ from: location }} />;
  return children;
}
