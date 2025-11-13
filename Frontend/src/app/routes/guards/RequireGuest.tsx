// src/app/routes/guards/RequireGuest.tsx
import { Navigate } from "react-router-dom";
import { useIsAuthenticated } from "@/features/auth/model/useAuthStore";

type Props = { children: React.ReactElement };

export default function RequireGuest({ children }: Props) {
  const authed = useIsAuthenticated();
  if (authed) return <Navigate to="/" replace />;
  return children;
}
