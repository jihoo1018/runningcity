import { RunIcon } from "@/shared/assets/icons";
import { EditIconButton } from "@/shared/ui/IconButtons";
import { UserLevelBadge } from "@/entities/user/ui/UserLevelBadge";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { useUserProfile } from "@/features/auth/model/selector";

const UserProfileHeader = () => {
  const { nickname, userId, totalExp } = useUserProfile();

  const { open } = useModalRouter();

  function onOpenUserModify() {
    open("user", "modify", { nickname, userId });
  }

  return (
    <div className="flex items-center gap-4 pt-1">
      {/* 유저프로필이미지(임시) */}
      <div className="border-custom-gray bg-custom-black relative flex h-16 w-16 items-center justify-center rounded-full border-2">
        <RunIcon className="text-custom-gray size-8" />
        <EditIconButton
          variant="round"
          className="bg-custom-gray text-custom-black absolute -right-1 -bottom-1 h-5 min-h-7 w-5 min-w-7 border-2"
          onClick={onOpenUserModify}
        />
      </div>
      <div className="flex flex-col gap-1">
        <p className="text-modal-title">{nickname ?? "noname"}</p>
        <UserLevelBadge totalExp={totalExp ?? 0} />
      </div>
    </div>
  );
};

export default UserProfileHeader;
