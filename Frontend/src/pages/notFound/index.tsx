import { WarningIcon } from "@/shared/assets/icons";
import { CommonLinkButton } from "@/shared/ui/CommonLinkButton";

export default function NotFoundPage() {
  return (
    <div className="flex h-full w-full flex-col items-center justify-center gap-5 p-5 text-center">
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-center gap-2">
          <WarningIcon className="size-10" />
          <h2 className="text-title">404</h2>
        </div>
        <p className="text-content">페이지를 찾을 수 없어요.</p>
      </div>

      <div className="w-[80%]">
        <CommonLinkButton href="/" variant="outline">
          메인으로 돌아가기
        </CommonLinkButton>
      </div>
    </div>
  );
}
