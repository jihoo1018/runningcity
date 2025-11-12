import { useModalRouter } from "@/app/modal/useModalRouter";
import { Modal } from "@/shared/ui";

export default function SettingConfirm({ onClose }: { onClose: () => void }) {
  const { back } = useModalRouter(); // 모달 내부 전용 뒤로가기
  return (
    <Modal open onClose={onClose} onBack={back}>
      <p>설정이 저장되었습니다.</p>
    </Modal>
  );
}
