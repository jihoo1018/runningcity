// src/pages/signup/index.tsx
import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import CommonButton from "@/shared/ui/CommonButton";
import { checkEmailAvailability, signup } from "@/features/auth/api/auth";

type EmailStatus = "idle" | "invalid" | "checking" | "available" | "taken" | "error";
type ConfirmStatus = "idle" | "match" | "mismatch";

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const SignupPage = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");

  const [emailStatus, setEmailStatus] = useState<EmailStatus>("idle");
  const [emailMessage, setEmailMessage] = useState("");
  const [emailFocused, setEmailFocused] = useState(false);

  const [passwordFocused, setPasswordFocused] = useState(false);
  const [confirmFocused, setConfirmFocused] = useState(false);
  const [confirmStatus, setConfirmStatus] = useState<ConfirmStatus>("idle");
  const [confirmTouched, setConfirmTouched] = useState(false);
  const [confirmMessage, setConfirmMessage] = useState("");

  const [formError, setFormError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const evaluateConfirm = (pwd: string, confirm: string): ConfirmStatus => {
    if (!confirm) return "idle";
    return pwd === confirm ? "match" : "mismatch";
  };

  const updateConfirmState = (pwd: string, confirm: string, touched?: boolean) => {
    const status = evaluateConfirm(pwd, confirm);
    setConfirmStatus(status);
    if (touched || confirmTouched) {
      setConfirmTouched(true);
      if (status === "mismatch") {
        setConfirmMessage("비밀번호가 일치하지 않습니다.");
      } else if (status === "match") {
        setConfirmMessage("비밀번호가 일치합니다.");
      } else {
        setConfirmMessage("");
      }
    }
  };

  const resetEmailStatus = () => {
    setEmailStatus("idle");
    setEmailMessage("");
  };

  const validateEmailFormat = (value: string) => {
    if (!value.trim()) {
      setEmailStatus("invalid");
      setEmailMessage("이메일을 입력해주세요.");
      return false;
    }
    if (!emailRegex.test(value.trim())) {
      setEmailStatus("invalid");
      setEmailMessage("이메일을 형식에 맞게 입력해주세요.");
      return false;
    }
    return true;
  };

  const checkEmail = async () => {
    if (!validateEmailFormat(email)) {
      return false;
    }
    setEmailStatus("checking");
    setEmailMessage("이메일을 확인하는 중입니다...");
    try {
      const res = await checkEmailAvailability(email.trim());
      if (res.data.exists) {
        setEmailStatus("taken");
        setEmailMessage("이미 사용중인 이메일입니다.");
        return false;
      }
      setEmailStatus("available");
      setEmailMessage("사용 가능한 이메일입니다.");
      return true;
    } catch (error) {
      setEmailStatus("error");
      setEmailMessage("이메일 중복 확인에 실패했습니다. 다시 시도해주세요.");
      return false;
    }
  };

  const handleEmailBlur = () => {
    setEmailFocused(false);
    void checkEmail();
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setFormError("");

    const emailValid = await checkEmail();
    if (!emailValid) return;

    if (!password) {
      setFormError("비밀번호를 입력해주세요.");
      return;
    }

    updateConfirmState(password, passwordConfirm, true);
    if (password !== passwordConfirm) {
      setFormError("비밀번호 확인이 일치하지 않습니다.");
      return;
    }

    setIsSubmitting(true);
    try {
      await signup({
        email: email.trim(),
        password,
        passwordConfirm,
      });
      navigate("/login", { replace: true });
    } catch (err: any) {
      const message = err?.response?.data?.message || "회원가입에 실패했습니다.";
      setFormError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const getEmailBorder = () => {
    if (emailStatus === "invalid" || emailStatus === "taken" || emailStatus === "error") {
      return "border-accent-red";
    }
    if (emailStatus === "available" || emailFocused || emailStatus === "checking") {
      return "border-primary";
    }
    return "border-custom-gray/60";
  };

  const getPasswordBorder = (focused: boolean) => {
    if (focused) return "border-primary";
    return "border-custom-gray/60";
  };

  const getConfirmBorder = () => {
    if (confirmStatus === "mismatch") return "border-accent-red";
    if (confirmStatus === "match") return "border-primary";
    if (confirmFocused) return "border-primary";
    return "border-custom-gray/60";
  };

  const getEmailMessageColor = () => {
    if (emailStatus === "invalid" || emailStatus === "taken" || emailStatus === "error") return "text-accent-red";
    if (emailStatus === "available") return "text-primary";
    return "text-custom-gray";
  };

  const getConfirmMessageColor = () => {
    if (confirmStatus === "mismatch") return "text-accent-red";
    if (confirmStatus === "match") return "text-primary";
    return "text-custom-gray";
  };

  return (
    <div className="min-h-screen w-full bg-section-bg flex items-center justify-center px-6">
      <div className="w-full max-w-xs">
        <h1 className="text-game-title text-custom-white text-center mb-10">RunningCity - SignUp</h1>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <input
              type="email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                resetEmailStatus();
              }}
              onFocus={() => setEmailFocused(true)}
              onBlur={handleEmailBlur}
              placeholder="이메일을 입력해주세요"
              className={`w-full bg-transparent text-custom-white placeholder:text-custom-gray outline-none border-b py-3 transition-colors ${getEmailBorder()}`}
              autoComplete="email"
              required
            />
            {emailMessage && (
              <p className={`mt-2 text-desc ${getEmailMessageColor()}`}>{emailMessage}</p>
            )}
          </div>

          <div>
            <input
              type="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                updateConfirmState(e.target.value, passwordConfirm);
              }}
              onFocus={() => setPasswordFocused(true)}
              onBlur={() => setPasswordFocused(false)}
              placeholder="비밀번호를 입력해주세요"
              className={`w-full bg-transparent text-custom-white placeholder:text-custom-gray outline-none border-b py-3 transition-colors ${getPasswordBorder(passwordFocused)}`}
              autoComplete="new-password"
              required
            />
          </div>

          <div>
            <input
              type="password"
              value={passwordConfirm}
              onChange={(e) => {
                setPasswordConfirm(e.target.value);
                updateConfirmState(password, e.target.value, true);
              }}
              onFocus={() => {
                setConfirmFocused(true);
                setConfirmTouched(true);
              }}
              onBlur={() => setConfirmFocused(false)}
              placeholder="비밀번호 확인을 해주세요"
              className={`w-full bg-transparent text-custom-white placeholder:text-custom-gray outline-none border-b py-3 transition-colors ${getConfirmBorder()}`}
              autoComplete="new-password"
              required
            />
            {confirmTouched && confirmMessage && (
              <p className={`mt-2 text-desc ${getConfirmMessageColor()}`}>{confirmMessage}</p>
            )}
          </div>

          {formError && (
            <p className="text-desc text-accent-red text-center" role="alert">
              {formError}
            </p>
          )}

          <CommonButton type="submit" variant="outline" disabled={isSubmitting}>
            {isSubmitting ? "가입 중..." : "회원가입"}
          </CommonButton>
        </form>
      </div>
    </div>
  );
};

export default SignupPage;

