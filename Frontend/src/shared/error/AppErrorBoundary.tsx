import { ErrorBoundary } from "react-error-boundary";
import { AppErrorFallback } from "./AppErrorFallback";

interface Props {
  children: React.ReactNode;
}

export function AppErrorBoundary({ children }: Props) {
  return (
    <ErrorBoundary
      FallbackComponent={AppErrorFallback}
      onError={(error, info) => {
        console.error("App Error:", error, info);
      }}
    >
      {children}
    </ErrorBoundary>
  );
}
