import type { FallbackProps } from "react-error-boundary";
import { CommonButton } from "../ui";
import { CommonLinkButton } from "../ui/CommonLinkButton";
import { WarningIcon } from "../assets/icons";

export function AppErrorFallback({ resetErrorBoundary }: FallbackProps) {
  return (
    <div className="flex h-screen w-full flex-col items-center justify-center gap-5 p-5 text-center">
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-center gap-2">
          <WarningIcon className="size-10" />
          <h2 className="text-title">오류 발생!</h2>
        </div>
        <p className="text-content">
          예상치 못한 오류가 발생했어요. <br />
          다시 한 번 시도해 주세요.
        </p>
      </div>
      <div className="flex w-[80%] flex-col gap-2">
        <CommonButton variant="solid" onClick={resetErrorBoundary}>
          다시 시도
        </CommonButton>
        <CommonLinkButton href="/" variant="outline">
          메인으로 돌아가기
        </CommonLinkButton>
      </div>
    </div>
  );
}
