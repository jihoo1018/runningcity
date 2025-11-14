import { NavButton } from "@/shared/ui";
import { BoutiqueIcon, HomeIcon, OfficeIcon, RecordIcon, RunIcon } from "@/shared/assets/icons";

const TABS = [
  { label: "부티크", to: "/boutique", icon: <BoutiqueIcon /> },
  { label: "잠입", to: "/entry", icon: <RunIcon /> },
  { label: "홈", to: "/", icon: <HomeIcon /> },
  { label: "쇼룸", to: "/showroom", icon: <OfficeIcon /> },
  { label: "기록", to: "/report", icon: <RecordIcon /> },
];

const Navbar = () => {
  return (
    <nav className="bg-custom-black border-custom-gray sticky bottom-0 left-0 z-10 flex h-20 w-full shrink-0 items-center justify-around border-t py-2">
      {TABS.map((tab) => (
        <NavButton
          key={tab.to}
          label={tab.label}
          link={tab.to}
          icon={tab.icon}
          className="w-[20%]"
        />
      ))}
    </nav>
  );
};

export default Navbar;
