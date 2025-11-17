import { ModalProps } from "@/app/modal/types";
import { Modal } from "@/shared/ui";

export default function PrivacySettingAlert({ onClose, payload }: ModalProps) {
  return (
    <Modal open title="태그 오류" onClose={onClose}>
      <p>최대 4개까지만 선택 가능합니다.</p>
    </Modal>
  );
}
