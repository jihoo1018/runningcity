import { getLevelInfo } from "@/entities/user/model/leveling";

export function UserLevelBadge({ totalExp }: { totalExp: number }) {
  const info = getLevelInfo(totalExp);

  return (
    <div className="flex w-40 flex-col gap-1">
      <div className="flex items-end justify-between">
        <span className="text-sm font-semibold">
          Lv.{info.level > 50 ? 50 : info.level}
          {info.level > 50 ? " (MAX)" : ""}
        </span>
        <span className="text-custom-gray text-xs">
          {info.expIntoLevel} / {info.needForNext}
        </span>
      </div>
      <div className="bg-custom-gray/50 h-1 w-full overflow-hidden">
        <div className="bg-primary h-full" style={{ width: `${info.progress * 100}%` }} />
      </div>
    </div>
  );
}
