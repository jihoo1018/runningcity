import { ModalProps } from "@/app/modal/types";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { Modal } from "@/shared/ui";

export default function UserConfirm({ onClose }: ModalProps) {
  const { back } = useModalRouter();

  return (
    <Modal open onClose={onClose} onBack={back}>
      <p>설정이 저장되었습니다.</p>
    </Modal>
  );
}
