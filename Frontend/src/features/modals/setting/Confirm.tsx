import { ModalProps } from "@/app/modal/types";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { Modal } from "@/shared/ui";

type GoalPayload = {
  target: number;
};

export default function SettingConfirm({ onClose, payload }: ModalProps) {
  const { back } = useModalRouter(); // 모달 내부 전용 뒤로가기

  const data = (payload as GoalPayload | undefined) ?? undefined;
  const target = data?.target;

  return (
    <Modal open onClose={onClose} onBack={back}>
      <p>설정이 저장되었습니다.</p>
      <p>Goal: {target}</p>
    </Modal>
  );
}
