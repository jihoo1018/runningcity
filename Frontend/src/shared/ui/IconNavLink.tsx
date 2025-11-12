import { forwardRef } from "react";
import { NavLink, NavLinkProps } from "react-router-dom";
import clsx from "clsx";

type Placement = "col" | "row";

type BaseProps = {
  icon: React.ReactNode;
  children?: React.ReactNode;
  placement?: Placement;
  gap?: number; // px
  loading?: boolean;
  disabled?: boolean;
  className?: string; // 컨테이너
  iconClassName?: string; // 아이콘
  textClassName?: string; // 텍스트
  "aria-label"?: string;
};

type IconNavLinkProps = BaseProps &
  Omit<NavLinkProps, "className" | "children"> & {
    to: NavLinkProps["to"];
    end?: boolean;
  };

function getLayout(placement: Placement) {
  return placement === "col" ? "flex-col" : "flex-row";
}

export const IconNavLink = forwardRef<HTMLAnchorElement, IconNavLinkProps>(
  (
    {
      icon,
      children,
      placement = "row",
      gap,
      loading = false,
      disabled = false,
      className,
      iconClassName,
      textClassName,
      ...rest
    },
    ref,
  ) => {
    const hasText = !!children;
    const needsAria = !hasText && !(rest as any)["aria-label"];
    const layout = getLayout(placement);

    const commonClass = clsx(
      "inline-flex items-center justify-center rounded-xl transition-colors",
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

    const textEl = hasText ? (
      <span className={clsx("text-nowrap", textClassName)}>{children}</span>
    ) : null;

    type VarStyles = React.CSSProperties & { ["--gap"]?: string };

    return (
      <NavLink
        ref={ref}
        {...rest}
        {...commonA11y}
        {...(gap != null ? { style: { ["--gap"]: `${gap}px` } as VarStyles } : {})}
        className={({ isActive }) =>
          clsx(
            commonClass,
            "text-desc text-custom-gray flex touch-manipulation gap-2 px-6 py-1 text-xs",
            isActive && "text-custom-white",
          )
        }
      >
        {iconEl}
        {textEl}
      </NavLink>
    );
  },
);
