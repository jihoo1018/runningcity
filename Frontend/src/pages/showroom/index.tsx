// src/pages/showroom/index.tsx
import { useState, useEffect } from "react";
import { ShowroomTabs } from "@/entities/showroom/ui/ShowroomTabs";
import { CharacterCard } from "@/entities/showroom/ui/CharacterCard";
import { ShowroomActions } from "@/entities/showroom/ui/ShowroomActions";
import { fetchGetShowroomMe } from "@/entities/showroom/api/me";
import { MyOffice } from "@/entities/showroom/model/type";
import { useShowroomLoader } from "@/entities/showroom/api/useShowroomLoader";
import { useAvatarStore } from "@/features/avatar/model/avatarStore";

export default function ShowroomPage() {
  const [tab, setTab] = useState<"me" | "friend" | "global">("me");
  const [myShowroom, setMyShowroom] = useState<MyOffice | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const { loadShowroom } = useShowroomLoader();
  const slots = useAvatarStore((s) => s.slots);

  useEffect(() => {
    const loadShowroom = async () => {
      try {
        setLoading(true);
        setError("");

        const data = await fetchGetShowroomMe();
        setMyShowroom(data);
      } catch (err: any) {
        console.error(err);
        setError("내 사무실 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    loadShowroom();
  }, []);

  return (
    <div className="flex flex-col">
      <ShowroomTabs tab={tab} setTab={setTab} />

      {loading && <div className="text-gray-400">불러오는 중...</div>}
      {error && <div className="text-red-400">{error}</div>}

      {!loading && !error && myShowroom && (
        <>
          <div className="flex items-start justify-center py-2">
            <CharacterCard tab={tab} data={myShowroom} />
          </div>

          <ShowroomActions tab={tab} />
        </>
      )}
    </div>
  );
}
