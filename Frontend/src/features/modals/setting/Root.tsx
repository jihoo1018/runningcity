// src/features/modals/setting/Root.tsx
import { useNavigate } from "react-router-dom";
import { useModalRouter } from "@/app/modal/useModalRouter";
import CommonButton from "@/shared/ui/CommonButton";
import { Modal } from "@/shared/ui";
import { ModalProps } from "@/app/modal/types";
import { useAuthStore } from "@/features/auth/model/useAuthStore";

export default function SettingRoot({ onClose }: ModalProps) {
  const { to } = useModalRouter();
  const navigate = useNavigate();
  const clearAuth = useAuthStore((s) => s.clear);

  const handleLogout = () => {
    clearAuth();
    onClose?.();
    navigate("/auth", { replace: true });
  };

  return (
    <Modal
      open
      onClose={onClose}
      title="환경설정"
      footer={
        <div className="flex w-full justify-end">
          <button onClick={() => to("setting", "withdraw")} className="text-desc">
            계정 삭제
          </button>
        </div>
      }
    >
      {/* 추후 효과음 관련 메뉴 추가 - 앱에 효과음 생기면 */}
      <div className="flex w-full flex-col gap-2">
        <CommonButton variant="outline">워치 연동하기</CommonButton>
        <CommonButton variant="outline">튜토리얼 다시보기</CommonButton>
        <CommonButton variant="outline" onClick={() => to("setting", "goal")}>
          목표 수정
        </CommonButton>
        <CommonButton variant="outline" onClick={handleLogout}>
          로그아웃
        </CommonButton>
      </div>
    </Modal>
  );
}
