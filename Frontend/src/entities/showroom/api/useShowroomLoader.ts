import { useAvatarStore } from "@/features/avatar/model/avatarStore";
import { fetchGetShowroomMe } from "@/entities/showroom/api/me";

export const useShowroomLoader = () => {
  const sync = useAvatarStore((s) => s.syncFromServer);

  const loadShowroom = async () => {
    const data = await fetchGetShowroomMe();
    sync(data.equippedItemList); // 서버 착장 → AvatarStore 저장
    return data;
  };

  return { loadShowroom };
};
