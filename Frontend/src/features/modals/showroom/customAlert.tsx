import { ModalProps } from "@/app/modal/types";
import { Modal } from "@/shared/ui";

type alertPayload = {
  msg: string;
};

export default function customAlert({ onClose, payload }: ModalProps) {
  const { msg } = payload as alertPayload;
  return (
    <Modal open title="옷 갈아입기" onClose={onClose}>
      {/* <p>현재 착장이 저장되었습니다.</p> */}
      <p>{msg}</p>
    </Modal>
  );
}
