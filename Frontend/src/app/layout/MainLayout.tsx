import { Navbar } from "@/widgets/navbar";
import { Outlet, useLocation } from "react-router-dom";
import homeBg from "@/shared/assets/images/home.png";
import { Suspense } from "react";
import { FullPageLoader } from "@/shared/ui/Loader";

export function MainLayout() {
  const location = useLocation();
  const isHome = location.pathname === "/";

  return (
    <div
      className="relative flex h-full min-h-screen w-full flex-col"
      style={
        isHome
          ? {
              backgroundImage: `url(${homeBg})`,
              backgroundSize: "cover",
              backgroundPosition: "center",
              backgroundRepeat: "no-repeat",
            }
          : undefined
      }
    >
      {isHome && (
        <div className="pointer-events-none absolute h-full w-full backdrop-brightness-125 backdrop-grayscale-50" />
      )}
      <main className="flex-1 overflow-y-auto p-5 pb-0">
        <Suspense fallback={<FullPageLoader />}>
          <Outlet />
        </Suspense>
      </main>
      <Navbar />
    </div>
  );
}
