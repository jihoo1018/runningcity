import { cn } from "../lib/cn";

type props = {
  variant: "outline" | "solid";
  children: React.ReactNode;
  className?: string;
} & React.ButtonHTMLAttributes<HTMLButtonElement>;

const CommonButton = ({ children, variant = "outline", className, ...rest }: props) => {
  return (
    <button
      {...rest}
      className={cn(
        "text-button flex w-full justify-center rounded-lg px-5 py-3",
        variant === "outline" &&
          "border-primary bg-section-bg active:bg-primary active:text-custom-black border",
        variant === "solid" && "bg-custom-white text-custom-black active:opacity-80",
        rest.disabled &&
          "border-custom-gray/50 text-custom-gray/50 bg-section-bg pointer-events-none cursor-not-allowed border",
        className,
      )}
    >
      {children}
    </button>
  );
};

export default CommonButton;
