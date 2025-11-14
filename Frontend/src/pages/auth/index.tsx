// src/pages/auth/index.tsx
import { useNavigate } from "react-router-dom";
import CommonButton from "@/shared/ui/CommonButton";

const AuthLandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen w-full bg-section-bg flex flex-col items-center justify-center px-6">
      <div className="w-full max-w-xs">
        <h1 className="text-game-title text-custom-white text-center mb-10">RunningCity</h1>

        <div className="space-y-4">
          <CommonButton variant="outline" onClick={() => navigate("/login")}>
            login
          </CommonButton>
          <CommonButton variant="outline" onClick={() => navigate("/signup")}>
            signup
          </CommonButton>
        </div>
      </div>
    </div>
  );
};

export default AuthLandingPage;

