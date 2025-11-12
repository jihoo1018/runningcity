import { forwardRef } from "react";
import clsx from "clsx";

type Placement = "col" | "row";

type BaseProps = {
  icon: React.ReactNode;
  children?: React.ReactNode; // 텍스트
  placement?: Placement; // 아이콘/텍스트 배치
  gap?: number; // 간격(px) 커스텀
  loading?: boolean;
  disabled?: boolean;
  className?: string; // 컨테이너 커스텀
  iconClassName?: string; // 아이콘만 커스텀
  textClassName?: string; // 텍스트만 커스텀
  "aria-label"?: string;
};

type ButtonLikeProps = {
  as?: "button";
} & React.ButtonHTMLAttributes<HTMLButtonElement>;

type AnchorLikeProps = {
  as: "a";
  href: string;
} & React.AnchorHTMLAttributes<HTMLAnchorElement>;

type IconActionProps = BaseProps & (ButtonLikeProps | AnchorLikeProps);

function getLayout(placement: Placement) {
  switch (placement) {
    case "col":
      return "flex-col";
    case "row":
    default:
      return "flex-row";
  }
}

export const IconAction = forwardRef<HTMLButtonElement | HTMLAnchorElement, IconActionProps>(
  function IconAction(
    {
      as = "button",
      icon,
      children,
      placement = "row",
      gap,
      loading = false,
      disabled,
      className,
      iconClassName,
      textClassName,
      ...rest
    },
    ref,
  ) {
    const hasText = !!children;
    const needsAria = !hasText && !(rest as any)["aria-label"];

    const layout = getLayout(placement);
    const styleGap = gap != null ? { gap: `${gap}px` } : undefined;

    const commonClass = clsx("inline-flex items-center justify-center", layout, className);

    const commonA11y = {
      ...(needsAria ? { "aria-label": "icon action" } : {}),
      ...(loading ? { "aria-busy": true } : {}),
    };

    const iconEl = (
      <span className={iconClassName} aria-hidden>
        {icon}
      </span>
    );

    const textEl = hasText ? <span className={textClassName}>{children}</span> : null;

    if (as === "a") {
      const anchorProps = rest as React.AnchorHTMLAttributes<HTMLAnchorElement>;
      return (
        <a
          ref={ref as React.Ref<HTMLAnchorElement>}
          className={commonClass}
          style={styleGap}
          {...commonA11y}
          {...anchorProps}
        >
          {iconEl}
          {textEl}
        </a>
      );
    }

    const buttonProps = rest as React.ButtonHTMLAttributes<HTMLButtonElement>;
    return (
      <button
        ref={ref as React.Ref<HTMLButtonElement>}
        type="button"
        disabled={disabled || loading}
        className={commonClass}
        style={styleGap}
        {...commonA11y}
        {...buttonProps}
      >
        {iconEl}
        {textEl}
      </button>
    );
  },
);

export type IconButtonProps = BaseProps & {
  as?: never;
} & React.ButtonHTMLAttributes<HTMLButtonElement>;

export const IconButton = forwardRef<HTMLButtonElement, IconButtonProps>((props, ref) => (
  <IconAction ref={ref} as="button" {...props} />
));

export type IconLinkProps = BaseProps & { as?: never; href: string } & Omit<
    React.AnchorHTMLAttributes<HTMLAnchorElement>,
    "href"
  >;

export const IconLink = forwardRef<HTMLAnchorElement, IconLinkProps>((props, ref) => (
  <IconAction ref={ref} as="a" {...props} />
));
