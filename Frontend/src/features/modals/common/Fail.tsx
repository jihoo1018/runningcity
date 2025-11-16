import { ModalProps } from "@/app/modal/types";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { Modal, CommonButton } from "@/shared/ui";
import { useNavigate } from "react-router-dom";

type ErrorPayload = {
  message: string;
};

export default function CommonFail({ onClose, payload }: ModalProps) {
  const { back } = useModalRouter();
  const navigate = useNavigate();

  const data = (payload as ErrorPayload | undefined) ?? undefined;
  const message = data?.message;

  const isOnboardingMessage = message?.includes("온보딩을 먼저 완료");

  const handleConfirm = () => {
    if (isOnboardingMessage) {
      onClose();
      navigate("/onboarding");
    } else {
      onClose();
    }
  };

  return (
    <Modal open onClose={onClose} onBack={back}>
      <div className="flex flex-col gap-4">
        <p className="text-content text-custom-white text-center">{message}</p>
        <CommonButton variant="solid" onClick={handleConfirm}>
          확인
        </CommonButton>
      </div>
    </Modal>
  );
}
