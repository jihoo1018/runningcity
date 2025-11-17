import CommonButton from "@/shared/ui/CommonButton";
import { Modal } from "@/shared/ui";
import { CommonLinkButton } from "@/shared/ui/CommonLinkButton";

export default function AlertRoot({ onClose }: { onClose: () => void }) {
  return (
    <Modal open onClose={onClose} title="알림">
      <div className="flex h-60 flex-col gap-2">
        {/* 알림 있을 경우 표시 */}
        <div className="border-primary/50 flex flex-col gap-4 border p-5 pt-6">
          <p className="w-full text-left">새로운 친구 신청이 있습니다.</p>
          <div className="flex gap-2">
            <CommonButton
              variant="outline"
              className="border-custom-gray text-label w-[50%] p-2"
              onClick={() => {}}
            >
              <span className="text-label">알림삭제</span>
            </CommonButton>
            <CommonLinkButton
              href="/friendship/request?tab=received"
              onClick={onClose}
              variant="solid"
              className="w-[50%] p-2"
            >
              <span className="text-label">이동</span>
            </CommonLinkButton>
          </div>
        </div>
      </div>
    </Modal>
  );
}
