import { IconButton as BaseIconButton, IconLink as BaseIconLink } from "./IconAction";
import {
  BackIcon,
  CloseIcon,
  NotificationIcon,
  QuestIcon,
  SettingIcon,
  SocialIcon,
} from "../assets/icons";
import { cn } from "../lib/cn";

type Variant = "ghost" | "round";
type Size = "sm" | "md" | "lg";

const SIZE: Record<Size, string> = {
  sm: "h-9 w-9 min-h-[44px] min-w-[44px]",
  md: "h-10 w-10 min-h-[44px] min-w-[44px]",
  lg: "h-12 w-12 min-h-[48px] min-w-[48px]",
};

function getVariantClass(variant: Variant) {
  return variant === "ghost"
    ? ["bg-transparent", "text-custom-gray"]
    : [
        "rounded-full border border-2",
        "bg-section-bg border-custom-gray/80 text-custom-gray shadow-primary/25 shadow-lg",
      ];
}

/* -------------------- BUTTON -------------------- */
type ButtonProps = React.ComponentProps<typeof BaseIconButton> & {
  variant?: Variant;
  size?: Size;
};

export const IconButton: React.FC<ButtonProps> = ({
  variant = "ghost",
  size = "md",
  className,
  ...rest
}) => {
  return (
    <BaseIconButton
      placement="row"
      gap={8}
      className={cn(
        "inline-flex items-center justify-center transition-colors",
        SIZE[size],
        ...getVariantClass(variant),
        className,
      )}
      {...rest}
    />
  );
};

/* -------------------- LINK -------------------- */
type LinkProps = React.ComponentProps<typeof BaseIconLink> & {
  variant?: Variant;
  size?: Size;
};

export const IconLinkButton: React.FC<LinkProps> = ({
  variant = "ghost",
  size = "md",
  className,
  ...rest
}) => {
  return (
    <BaseIconLink
      placement="row"
      gap={8}
      className={cn(
        "inline-flex items-center justify-center transition-colors",
        SIZE[size],
        ...getVariantClass(variant),
        className,
      )}
      {...rest}
    />
  );
};

/** 명시적 이름 프리셋 */
export const BackIconButton = (p: Omit<ButtonProps, "icon" | "aria-label">) => (
  <IconButton icon={<BackIcon />} aria-label="뒤로가기" {...p} />
);

export const CloseIconButton = (p: Omit<ButtonProps, "icon" | "aria-label">) => (
  <IconButton icon={<CloseIcon />} aria-label="닫기" {...p} />
);

export const SettingIconButton = (p: Omit<ButtonProps, "icon" | "aria-label">) => (
  <IconButton icon={<SettingIcon />} aria-label="환경설정" {...p} />
);

export const AlertIconButton = (p: Omit<ButtonProps, "icon" | "aria-label">) => (
  <IconButton icon={<NotificationIcon />} aria-label="알림" {...p} />
);

export const QuestIconButton = (p: Omit<ButtonProps, "icon" | "aria-label">) => (
  <IconButton icon={<QuestIcon />} aria-label="퀘스트" {...p} />
);

export const FriendshipIconLink = (p: Omit<LinkProps, "icon" | "aria-label">) => (
  <IconLinkButton icon={<SocialIcon />} aria-label="친구" {...p} />
);
