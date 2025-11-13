import { useModalRouter } from "@/app/modal/useModalRouter";
import CommonButton from "@/shared/ui/CommonButton";
import { Modal } from "@/shared/ui";

export default function EntryRoot({ onClose }: { onClose: () => void }) {
  const { to } = useModalRouter();
  return (
    <Modal
      open
      onClose={onClose}
      title="잠입기지"
      footer={
        <CommonButton variant="outline" onClick={() => to("setting", "confirm")}>
          저장하기
        </CommonButton>
      }
    >
      <p>기타 설정</p>
    </Modal>
  );
}
