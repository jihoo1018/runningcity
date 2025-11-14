// src/pages/login/index.tsx
import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import CommonButton from "@/shared/ui/CommonButton";
import { login } from "@/features/auth/api/auth";
import { useAuthStore } from "@/features/auth/model/useAuthStore";

const LoginPage = () => {
  const navigate = useNavigate();
  const setUser = useAuthStore((s) => s.setUser);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const res = await login({ email, password });
      const d = res.data;
      // store in zustand
      setUser({
        userId: d.userId,
        nickname: d.userNickname,
        userCode: d.userCode,
        totalExp: d.totalexp,
      });
      navigate("/", { replace: true });
    } catch (err: any) {
      const msg = err?.response?.data?.message || err.message || "로그인에 실패했습니다.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-full bg-section-bg flex items-center justify-center px-6">
      <div className="w-full max-w-xs">
        <h1 className="text-game-title text-custom-white text-center mb-10">RunningCity - Login</h1>

        <form onSubmit={onSubmit} className="space-y-6">
          <label className="block">
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="이메일을 입력하세요"
              className="w-full bg-transparent text-custom-white placeholder:text-custom-gray outline-none border-b border-custom-gray/60 py-3"
              autoComplete="email"
              required
            />
          </label>
          <label className="block">
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력하세요"
              className="w-full bg-transparent text-custom-white placeholder:text-custom-gray outline-none border-b border-custom-gray/60 py-3"
              autoComplete="current-password"
              required
            />
          </label>

          {error && (
            <p className="text-desc text-accent-red text-center" role="alert">
              {error}
            </p>
          )}

          <CommonButton type="submit" variant="outline" disabled={loading}>
            {loading ? "요청 중..." : "로그인"}
          </CommonButton>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
