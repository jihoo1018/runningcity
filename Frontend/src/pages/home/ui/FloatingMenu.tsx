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
    <aside className="fixed top-6 right-5 z-10 flex flex-col gap-3">
      <SettingIconButton variant="round" size="sm" onClick={() => open("setting", "root")} />
      <FriendshipIconLink variant="round" size="sm" to="/friendship" />
      <AlertIconButton variant="round" size="sm" onClick={() => open("alert", "root")} />
      {/* 일일퀘스트 연결 */}
      <QuestIconButton variant="round" size="sm" onClick={() => {}} />
    </aside>
  );
};
