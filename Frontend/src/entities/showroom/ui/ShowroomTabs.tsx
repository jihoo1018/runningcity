type Props = {
  tab: "me" | "friend" | "global";
  setTab: (t: "me" | "friend" | "global") => void;
};

export const ShowroomTabs = ({ tab, setTab }: Props) => {
  const tabStyle = (key: string) =>
    `px-4 py-2 rounded-lg text-sm ${
      tab === key ? "bg-primary text-custom-black" : "border border-custom-gray text-custom-gray"
    }`;

  return (
    <div className="flex justify-center gap-2">
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
