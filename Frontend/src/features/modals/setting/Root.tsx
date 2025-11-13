import { useModalRouter } from "@/app/modal/useModalRouter";
import CommonButton from "@/shared/ui/CommonButton";
import { Modal } from "@/shared/ui";
import { ModalProps } from "@/app/modal/types";

export default function SettingRoot({ onClose }: ModalProps) {
  const { to } = useModalRouter();

  return (
    <Modal
      open
      onClose={onClose}
      title="환경설정"
      footer={
        <div className="flex w-full justify-end">
          <button onClick={() => to("setting", "withdraw")} className="text-desc">
            계정 삭제
          </button>
        </div>
      }
    >
      {/* 추후 효과음 관련 메뉴 추가 - 앱에 효과음 생기면 */}
      <div className="flex w-full flex-col gap-2">
        <CommonButton variant="outline">워치 연동하기</CommonButton>
        <CommonButton variant="outline">튜토리얼 다시보기</CommonButton>
        <CommonButton variant="outline" onClick={() => to("setting", "goal")}>
          목표 수정
        </CommonButton>
      </div>
    </Modal>
  );
}
