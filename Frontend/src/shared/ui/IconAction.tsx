import { forwardRef } from "react";
import clsx from "clsx";
import { Link } from "react-router-dom";

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

type LinkLikeProps = {
  as: "link";
  to: string;
} & Omit<React.ComponentProps<typeof Link>, "to" | "className" | "children">;

type IconActionProps = BaseProps & (ButtonLikeProps | LinkLikeProps);

function getLayout(placement: Placement) {
  return placement === "col" ? "flex-col" : "flex-row";
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

    const commonClass = clsx(
      "inline-flex items-center justify-center",
      layout,
      (disabled || loading) && "opacity-60 pointer-events-none",
      className,
    );

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

    if (as === "link") {
      const { to, onClick, ...linkProps } = rest as LinkLikeProps;

      const handleClick: React.MouseEventHandler<HTMLAnchorElement> = (e) => {
        if (disabled || loading) {
          e.preventDefault();
          e.stopPropagation();
          return;
        }
        onClick?.(e);
      };

      return (
        <Link
          ref={ref as any}
          to={to}
          onClick={handleClick}
          aria-disabled={disabled || loading ? true : undefined}
          tabIndex={disabled || loading ? -1 : undefined}
          className={commonClass}
          style={styleGap}
          {...commonA11y}
          {...linkProps}
        >
          {iconEl}
          {textEl}
        </Link>
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

export type IconButtonProps = BaseProps &
  Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, "className" | "children"> & {
    as?: never;
  };

export const IconButton = forwardRef<HTMLButtonElement, IconButtonProps>((props, ref) => (
  <IconAction ref={ref} as="button" {...props} />
));

export type IconLinkProps = BaseProps &
  Omit<React.ComponentProps<typeof Link>, "to" | "className" | "children"> & {
    as?: never;
    to: string;
  };

export const IconLink = forwardRef<HTMLAnchorElement, IconLinkProps>((props, ref) => (
  <IconAction ref={ref} as="link" {...props} />
));
