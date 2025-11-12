import { useModalRouter } from "@/app/modal/useModalRouter";
import { SettingIconButton } from "@/shared/ui/IconButtons";

export const FloatingMenu = () => {
  const { open } = useModalRouter();

  return (
    <aside className="fixed top-6 right-4 z-40 flex flex-col gap-3">
      <SettingIconButton variant="round" size="sm" onClick={() => open("setting", "root")} />
      {/* <button className="btn-circle" onClick={() => open("setting", "root")} aria-label="설정">
        <GearIcon />
      </button>
      <button className="btn-circle" onClick={() => open("report", "preview")} aria-label="리포트">
        <ClipboardIcon />
      </button>
      <button className="btn-circle" onClick={() => open("settings", "confirm")} aria-label="알림">
        <BellIcon />
      </button>
      <button className="btn-circle" onClick={() => open("settings", "root")} aria-label="친구">
        <UserIcon />
      </button> */}
    </aside>
  );
};
