import { useModalRouter } from "@/app/modal/useModalRouter";
import CommonButton from "@/shared/ui/CommonButton";
import { Modal } from "@/shared/ui";

export default function PrivacySettingRoot({ onClose }: { onClose: () => void }) {
  const { to } = useModalRouter();
  return (
    <Modal
      open
      onClose={onClose}
      title="공개 설정"
      footer={
        <CommonButton variant="outline" onClick={() => to("privacySetting", "confirm")}>
          저장하기
        </CommonButton>
      }
    >
      <p>기타 설정</p>
    </Modal>
  );
}
