import { NavButton } from "@/shared/ui";
import {
  BoutiqueIcon,
  HomeIcon,
  OfficeIcon,
  RecordIcon,
  RunIcon,
} from "@/shared/assets/icons";

const TABS = [
  { label: "부티크", to: "/boutique", icon: <BoutiqueIcon /> },
  { label: "잠입", to: "/entry", icon: <RunIcon /> },
  { label: "홈", to: "/", icon: <HomeIcon /> },
  { label: "쇼룸", to: "/showroom", icon: <OfficeIcon /> },
  { label: "기록", to: "/report", icon: <RecordIcon /> },
];

const Navbar = () => {
  return (
    <nav className="bg-custom-black border-t border-custom-gray flex justify-around items-center py-2 shrink-0 z-10">
      {TABS.map((tab) => (
        <NavButton
          key={tab.to}
          label={tab.label}
          link={tab.to}
          icon={tab.icon}
        />
      ))}
    </nav>
  );
};

export default Navbar;
