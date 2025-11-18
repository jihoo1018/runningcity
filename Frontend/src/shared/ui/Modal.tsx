import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { BackIconButton, CloseIconButton } from "./IconButtons";
import clsx from "clsx";
import { createPortal } from "react-dom";

type ModalProps = {
  open: boolean;
  onClose: () => void;
  onBack?: () => void; // 뒤로가기 버튼
  title?: React.ReactNode;
  size?: "sm" | "md" | "lg" | "xl";
  closeOnBackdrop?: boolean; // 백드롭 클릭 닫기 여부
  footer?: React.ReactNode;
  className?: string;
  children: React.ReactNode;
};

const SIZE_CLASS: Record<NonNullable<ModalProps["size"]>, string> = {
  sm: "max-w-sm",
  md: "max-w-md",
  lg: "max-w-lg",
  xl: "max-w-2xl",
};

function usePortalTarget(id = "modal-root") {
  const [el, setEl] = useState<HTMLElement | null>(null);
  useLayoutEffect(() => {
    let target = document.getElementById(id);
    if (!target) {
      target = document.createElement("div");
      target.id = id;
      document.body.appendChild(target);
    }
    setEl(target);
  }, [id]);
  return el;
}

function useLockBodyScroll(locked: boolean) {
  useEffect(() => {
    if (!locked) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, [locked]);
}

export const Modal: React.FC<ModalProps> = ({
  open,
  onClose,
  onBack,
  title,
  size = "md",
  closeOnBackdrop = true,
  footer,
  className,
  children,
}) => {
  const portal = usePortalTarget("modal-root");
  const backdropRef = useRef<HTMLDivElement>(null);
  const panelRef = useRef<HTMLDivElement>(null);

  useLockBodyScroll(open);

  if (!portal || !open) return null;

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (!closeOnBackdrop) return;
    if (e.target === backdropRef.current) onClose();
  };

  const node = (
    <div
      ref={backdropRef}
      onMouseDown={handleBackdropClick}
      className={clsx(
        "fixed inset-0 z-[100] bg-black/50 backdrop-blur-sm",
        "flex items-center justify-center",
        "px-8 pt-[max(env(safe-area-inset-top),24px)] pb-[max(env(safe-area-inset-bottom),24px)]",
        "pt-[max(env(safe-area-inset-top),24px)] pb-[max(env(safe-area-inset-bottom),24px)]",
      )}
      aria-label="modal-backdrop"
    >
      <div
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? "modal-title" : undefined}
        className={clsx(
          "flex max-h-[70%] w-full flex-col py-5",
          "bg-section-bg border-primary shadow-primary/20 items-center border shadow-md",
          "outline-none focus:outline-none",
          "overflow-hidden",
          SIZE_CLASS[size],
          "animate-[modalIn_160ms_ease-out]",
          className,
        )}
        onMouseDown={(e) => e.stopPropagation()}
      >
        <div className="flex w-full items-center gap-2 px-3 pb-2">
          {onBack ? (
            <BackIconButton variant="ghost" onClick={onBack} />
          ) : (
            <span className="h-10 w-10" aria-hidden />
          )}
          <div className="flex-1 text-center">
            {title ? <h2 className="text-modal-title">{title}</h2> : null}
          </div>

          <CloseIconButton variant="ghost" onClick={onClose} />
        </div>

        <div className="text-content flex w-full flex-1 flex-col overflow-hidden px-5 py-4 text-center">
          {children}
        </div>
        {footer && (
          <div className="w-full px-5 py-3 pb-[max(env(safe-area-inset-bottom),12px)]">
            {footer}
          </div>
        )}
      </div>

      {/* 키프레임 */}
      <style>{`
        @keyframes modalIn {
          0% { opacity: 0; transform: translateY(8px) scale(0.98) }
          100% { opacity: 1; transform: translateY(0) scale(1) }
        }
      `}</style>
    </div>
  );

  return createPortal(node, portal);
};
