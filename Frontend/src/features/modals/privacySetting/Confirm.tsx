import { ModalProps } from "@/app/modal/types";
import { Modal } from "@/shared/ui";

export default function PrivacySettingConfirm({ onClose, payload }: ModalProps) {
  return (
    <Modal open title="공개 설정" onClose={onClose}>
      <p>설정이 저장되었습니다.</p>
    </Modal>
  );
}
