type Props = {
  tab: "me" | "friend" | "global";
  setTab: (t: "me" | "friend" | "global") => void;
};
export const ShowroomTabs = ({ tab, setTab }: Props) => {
  const tabStyle = (key: string) =>
    `
      px-3 py-2 sm:px-4 sm:py-2 
      rounded-lg text-buton
      ${tab === key ? "bg-primary text-black" : "border border-custom-gray bg-section-bg text-custom-gray"}
    `;

  return (
    <div className="mt-2 flex justify-center gap-1 sm:gap-2">
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
