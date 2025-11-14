import { Link } from "react-router-dom";
import { cn } from "../lib/cn";

type LinkButtonProps = {
  variant?: "outline" | "solid";
  children: React.ReactNode;
  className?: string;
  href: string;
} & React.AnchorHTMLAttributes<HTMLAnchorElement>;

export const CommonLinkButton = ({
  children,
  variant = "outline",
  className,
  href,
  ...rest
}: LinkButtonProps) => {
  return (
    <Link
      to={href}
      {...rest}
      className={cn(
        "text-button flex w-full justify-center rounded-lg px-5 py-3 transition-colors",
        variant === "outline" &&
          "border-primary bg-section-bg active:bg-primary active:text-custom-black border",
        variant === "solid" && "bg-custom-white text-custom-black active:opacity-80",
        rest["aria-disabled"] &&
          "border-custom-gray/50 text-custom-gray/50 bg-section-bg pointer-events-none cursor-not-allowed border",
        className,
      )}
    >
      {children}
    </Link>
  );
};
