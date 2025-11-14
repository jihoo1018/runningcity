// src/features/modals/mission/Confirm.tsx
import React from "react";
import { Modal } from "@/shared/ui/Modal";
import { useModalRouter } from "@/app/modal/useModalRouter";
import type { ModalProps } from "@/app/modal/types";

type Payload = { date?: string };

export default function MissionConfirm({ onClose, payload }: ModalProps) {
  const { back } = useModalRouter();
  const { date } = (payload as Payload) ?? {};

  return (
    <Modal open onClose={onClose} onBack={back} title="보상 수령 완료">
      <p style={{ margin: 0, textAlign: "center" }}>
        {date ? `${date} 미션 보상을 수령했습니다.` : "미션 보상을 수령했습니다."}
      </p>
    </Modal>
  );
}
