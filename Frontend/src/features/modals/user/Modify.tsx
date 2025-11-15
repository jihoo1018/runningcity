import CommonButton from "@/shared/ui/CommonButton";
import { Modal } from "@/shared/ui";
import { ModalProps } from "@/app/modal/types";
import { RunIcon } from "@/shared/assets/icons";
import { useEffect, useState } from "react";
import { updateNickname } from "@/entities/user/api";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { updateUser } from "@/features/auth/model/actions";

type NicknamePayload = {
  nickname: string;
  userId: number;
};

export default function UserModify({ onClose, payload }: ModalProps) {
  const { nickname: prevName = "", userId } = (payload ?? {}) as NicknamePayload;

  const [nickname, setNickname] = useState(prevName);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const { to } = useModalRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 닉네임 유효성 검사
    if (!nickname.trim()) {
      setError("닉네임을 입력해 주세요");
      return;
    }

    if (nickname.length < 2) {
      setError("닉네임은 2글자 이상이어야 합니다");
      return;
    }

    if (nickname.length > 10) {
      setError("닉네임은 10글자 이하여야 합니다");
      return;
    }

    // 한글, 영문, 숫자만 허용
    const nicknamePattern = /^[a-zA-Z0-9가-힣]+$/;
    if (!nicknamePattern.test(nickname)) {
      setError("닉네임은 한글, 영문, 숫자만 사용 가능합니다");
      return;
    }

    setIsLoading(true);
    setError("");

    try {
      const result = await updateNickname(userId, nickname.trim());
      updateUser({ nickname });
      to("user", "confirm");
      console.log("닉네임 업데이트 완료:", result);
    } catch (err: any) {
      // API 에러 응답 처리
      if (err.response?.data) {
        const errorData = err.response.data;

        // 닉네임 중복 (409)
        if (errorData.code === "USER_4090") {
          setError("이미 사용 중인 닉네임입니다. 다른 닉네임을 입력해주세요.");
          return;
        }

        // 유효성 검증 실패 (400)
        if (errorData.code === "USER_4000" && errorData.error?.details) {
          const fieldErrors = errorData.error.details
            .map((detail: any) => detail.message)
            .join(", ");
          setError(fieldErrors);
          return;
        }

        // 기타 에러
        to("common", "fail", {
          message: errorData.message ?? "닉네임 업데이트 중 오류가 발생했습니다.",
        });
      } else {
        to("common", "fail", {
          message: "네트워크 오류가 발생했습니다. 다시 시도해 주세요.",
        });
      }

      console.error("닉네임 업데이트 실패:", err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal open onClose={onClose} title="정보 수정" closeOnBackdrop={false}>
      <form className="flex w-full flex-col items-center gap-3" onSubmit={handleSubmit}>
        <div className="border-custom-gray bg-custom-black relative flex h-16 w-16 items-center justify-center rounded-full border-2">
          <RunIcon className="text-custom-gray size-8" />
        </div>
        <div className="flex w-full flex-col gap-2">
          <div className="flex items-center justify-between">
            <label className="text-label text-custom-white block">닉네임</label>
            <span className="text-desc text-custom-gray">{nickname.length}/10</span>
          </div>
          <div className="relative pb-7">
            <input
              type="text"
              value={nickname}
              onChange={(e) => {
                setNickname(e.target.value);
                setError(""); // 입력 시 에러 초기화
              }}
              placeholder="닉네임을 입력해 주세요"
              maxLength={10}
              disabled={isLoading}
              className={`text-content box-border w-full rounded-lg border p-3 transition-colors duration-200 outline-none ${
                error
                  ? "border-accent-red bg-custom-black text-custom-white"
                  : isLoading
                    ? "border-custom-gray bg-section-bg text-custom-gray opacity-50"
                    : "border-custom-gray bg-custom-black text-custom-white focus:border-primary"
              }`}
            />
            {error && (
              <span className="text-desc text-accent-red absolute right-0 bottom-1 rounded-lg">
                {error}
              </span>
            )}
          </div>
        </div>
        <CommonButton variant="solid" disabled={isLoading} type="submit" className="font-bold">
          {isLoading ? "처리 중..." : "확인"}
        </CommonButton>
      </form>
    </Modal>
  );
}
