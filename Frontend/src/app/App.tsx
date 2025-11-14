console.log("✅ React App Loaded");

import { HashRouter } from "react-router-dom";
import { Providers } from "./providers";
import { AppRoutes } from "./routes";
import { AppErrorBoundary } from "@/shared/error/AppErrorBoundary";

function App() {
  return (
    <Providers>
      <HashRouter>
        <AppErrorBoundary>
          <AppRoutes />
        </AppErrorBoundary>
      </HashRouter>
    </Providers>
  );
}

export default App;
