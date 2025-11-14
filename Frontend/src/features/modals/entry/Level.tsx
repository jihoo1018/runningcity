import { useModalRouter } from "@/app/modal/useModalRouter";
import CommonButton from "@/shared/ui/CommonButton";
import { Modal } from "@/shared/ui";

export default function EntryLevel({ onClose }: { onClose: () => void }) {
  const { to } = useModalRouter();
  return (
    <Modal
      open
      onClose={onClose}
      title="난이도 선택"
      // footer={
      //   <CommonButton variant="outline" onClick={() => to("entry", "level")}>
      //     저장하기
      //   </CommonButton>
      // }
    >
      <p>기타 설정</p>
    </Modal>
  );
}
