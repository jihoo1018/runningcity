import { memo } from "react";
import { SpinnerIcon } from "../assets/icons";

export const Loader = () => {
  return <SpinnerIcon className="text-primary size-10 animate-spin rounded-full" />;
};

export const FullPageLoader = memo(() => {
  return (
    <div className="relative flex h-full w-full items-center justify-center">
      <Loader />
    </div>
  );
});
