type Props = {
  tab: "me" | "friend" | "global";
  setTab: (t: "me" | "friend" | "global") => void;
};
export const ShowroomTabs = ({ tab, setTab }: Props) => {
  const tabStyle = (key: string) =>
    `
      px-4 py-2 rounded-xl font-semibold transition-all
      ${
        tab === key
          ? "bg-cyan-400 text-black shadow-[0_0_10px_rgba(0,255,255,0.8)]"
          : "border border-custom-gray text-custom-gray hover:border-cyan-300"
      }
    `;

  return (
    <div className="flex justify-center gap-3">
      <button className={tabStyle("me")} onClick={() => setTab("me")}>
        내 사무실
      </button>
      <button className={tabStyle("friend")} onClick={() => setTab("friend")}>
        친구
      </button>
      <button className={tabStyle("global")} onClick={() => setTab("global")}>
        글로벌
      </button>
    </div>
  );
};
