import { useModalRouter } from "@/app/modal/useModalRouter";
import CommonButton from "@/shared/ui/CommonButton";
import { Modal } from "@/shared/ui";

export default function SettingRoot({ onClose }: { onClose: () => void }) {
  const { to } = useModalRouter();
  return (
    <Modal
      open
      onClose={onClose}
      title="환경설정"
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
