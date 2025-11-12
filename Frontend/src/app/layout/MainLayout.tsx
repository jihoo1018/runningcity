import { Navbar } from "@/widgets/navbar";
import { Outlet } from "react-router-dom";

export function MainLayout() {
  return (
    <div className="flex h-full min-h-screen w-full flex-col">
      <main className="flex-1 overflow-y-auto p-5">
        <Outlet />
      </main>
      <Navbar />
    </div>
  );
}
