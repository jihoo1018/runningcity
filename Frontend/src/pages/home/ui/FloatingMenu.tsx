import { useModalRouter } from "@/app/modal/useModalRouter";
import {
  AlertIconButton,
  FriendshipIconLink,
  QuestIconButton,
  SettingIconButton,
} from "@/shared/ui/IconButtons";

export const FloatingMenu = () => {
  const { open } = useModalRouter();

  return (
    <aside className="fixed top-6 right-5 z-40 flex flex-col gap-3">
      <SettingIconButton variant="round" size="sm" onClick={() => open("setting", "root")} />
      <FriendshipIconLink variant="round" size="sm" href="/friendship" />
      <AlertIconButton variant="round" size="sm" onClick={() => {}} />
      <QuestIconButton variant="round" size="sm" onClick={() => {}} />
    </aside>
  );
};
