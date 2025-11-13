import clsx from "clsx";
import { IconButton as BaseIconButton } from "./IconAction";
import { BackIcon, CloseIcon, SettingIcon } from "../assets/icons";

type Variant = "ghost" | "round";
type Size = "sm" | "md" | "lg";

const SIZE: Record<Size, string> = {
  sm: "h-9 w-9 min-h-[44px] min-w-[44px]",
  md: "h-10 w-10 min-h-[44px] min-w-[44px]",
  lg: "h-12 w-12 min-h-[48px] min-w-[48px]",
};

type Props = React.ComponentProps<typeof BaseIconButton> & {
  variant?: Variant;
  size?: Size;
};

export const IconButton: React.FC<Props> = ({
  variant = "ghost",
  size = "md",
  className,
  ...rest
}) => {
  const base = ["inline-flex items-center justify-center transition-colors", SIZE[size]];

  const variantCls =
    variant === "ghost"
      ? ["bg-transparent", "text-custom-gray"]
      : ["rounded-full border", "bg-section-bg border-primary text-custom-gray"];

  return (
    <BaseIconButton
      placement="row"
      gap={8}
      className={clsx(...base, ...variantCls, className)}
      {...rest}
    />
  );
};

/** 명시적 이름 프리셋 */
export const BackIconButton = (p: Omit<Props, "icon" | "aria-label">) => (
  <IconButton icon={<BackIcon />} aria-label="뒤로가기" {...p} />
);
export const CloseIconButton = (p: Omit<Props, "icon" | "aria-label">) => (
  <IconButton icon={<CloseIcon />} aria-label="닫기" {...p} />
);

export const SettingIconButton = (p: Omit<Props, "icon" | "aria-label">) => (
  <IconButton icon={<SettingIcon />} aria-label="환경설정" {...p} />
);
