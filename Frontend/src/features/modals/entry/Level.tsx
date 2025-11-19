// src/features/modals/entry/Level.tsx

import { Modal } from "@/shared/ui";
import { useNavigate } from "react-router-dom";
import { fetchStartEntry } from "@/entities/entry/api";
import type { ModalProps } from "@/app/modal/types";

export default function EntryLevel({ onClose, payload }: ModalProps) {
  const navigate = useNavigate();

  const { entryName, baseId, userId } = payload as {
    entryName: string;
    baseId: number;
    userId: number;
  };

  const handleStart = async () => {
    try {
      const data = await fetchStartEntry(userId, baseId);

      if (window.Android?.startRunning) {
        window.Android.startRunning(String(data.sessionId));
      }

      onClose();
      navigate("/running", { state: { sessionId: data.sessionId, mode: 'entry' } });
    } catch (e) {
      console.error("세션 생성 실패", e);
    }
  };

  return (
    <Modal open onClose={onClose} title={entryName}>
      <div className="flex flex-col items-center justify-center gap-3 px-3 py-2">
        <p className="text-[12px] text-[#E6FFFF] mb-1">
          목표 기지 접근 허가됨. 잠입을 개시하세요👇
        </p>

        <button
          onClick={handleStart}
          className="
            w-full py-3 rounded-md 
            bg-[#00E6FF] text-[#1D2330] 
            font-bold 
            active:scale-[0.98] transition
          "
        >
          🔹 잠입 시작하기
        </button>
      </div>
    </Modal>
  );
}
