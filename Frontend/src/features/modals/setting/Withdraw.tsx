import { useModalRouter } from "@/app/modal/useModalRouter";
import { CommonButton, Modal } from "@/shared/ui";

export default function SettingWithdraw({ onClose }: { onClose: () => void }) {
  const { back } = useModalRouter();
  return (
    <Modal
      open
      onClose={onClose}
      title="계정 삭제"
      onBack={back}
      footer={
        <CommonButton
          variant="outline"
          className="border-custom-gray! text-custom-gray"
          onClick={() => {
            // 회원 탈퇴 요청 추가
          }}
        >
          확인
        </CommonButton>
      }
    >
      <p>
        삭제한 정보는 복구되지 않아요. <br />
        정말 삭제하시겠어요?
      </p>
    </Modal>
  );
}
