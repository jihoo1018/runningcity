import { ModalProps } from "@/app/modal/types";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { Modal } from "@/shared/ui";

type ErrorPayload = {
  message: string;
};

export default function CommonFail({ onClose, payload }: ModalProps) {
  const { back } = useModalRouter();

  const data = (payload as ErrorPayload | undefined) ?? undefined;
  const message = data?.message;

  return (
    <Modal open onClose={onClose} onBack={back}>
      <p>${message}</p>
    </Modal>
  );
}
