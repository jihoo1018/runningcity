// src/features/modals/setting/Root.tsx
import { useNavigate } from "react-router-dom";
import { useModalRouter } from "@/app/modal/useModalRouter";
import CommonButton from "@/shared/ui/CommonButton";
import { Modal } from "@/shared/ui";
import { ModalProps } from "@/app/modal/types";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import { useWatchPairing } from "./useWatchPairing";

export default function SettingRoot({ onClose }: ModalProps) {
  const { to } = useModalRouter();
  const navigate = useNavigate();
  const clearAuth = useAuthStore((s) => s.clear);
  const { isPairing, pairingMessage, pairWatch } = useWatchPairing();

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
        <CommonButton 
          variant="outline" 
          onClick={pairWatch}
          disabled={isPairing}
        >
          {isPairing ? "연동 중..." : "워치 연동하기"}
        </CommonButton>
        {pairingMessage && (
          <div className={`text-desc rounded-lg border p-2 ${
            pairingMessage.includes("완료") 
              ? "text-accent-green border-accent-green bg-[rgba(76,175,80,0.1)]"
              : pairingMessage.includes("실패") || pairingMessage.includes("초과")
              ? "text-accent-red border-accent-red bg-[rgba(255,73,53,0.1)]"
              : "text-custom-gray border-custom-gray bg-section-bg"
          }`}>
            {pairingMessage}
          </div>
        )}
        <CommonButton variant="outline" onClick={() => to('setting', 'tutorial')}>
          튜토리얼 다시보기
        </CommonButton>
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
