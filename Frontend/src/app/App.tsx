console.log("✅ React App Loaded");

import { HashRouter } from "react-router-dom";
import { Providers } from "./providers";
import { AppRoutes } from "./routes";

function App() {
  return (
    <Providers>
      <HashRouter>
        <AppRoutes />
      </HashRouter>
    </Providers>
  );
}

export default App;
