// src/widgets/navbar/ui/Navbar.tsx

import { NavButton } from "../../../shared/ui";
import { AndroidBridge } from "../../../shared/lib/webview";
import { useNavigate } from "react-router-dom";
interface NavbarProps {
  activeTab?: string;
}

const Navbar = ({ activeTab = "홈" }: NavbarProps) => {
  const navigate = useNavigate();
  const handleNavClick = (label: string) => {
    AndroidBridge.showToast(label);
  };

  return (
    <div
      style={{
        backgroundColor: "white",
        borderTop: "1px solid #e5e7eb",
        padding: "8px 0",
        display: "flex",
        justifyContent: "space-around",
        alignItems: "center",
        boxShadow: "0 -2px 10px rgba(0,0,0,0.05)",
        flexShrink: 0,
      }}
    >
      <NavButton
        label="부티크"
        onClick={() => handleNavClick("부티크")}
        isActive={activeTab === "부티크"}
      />
      <NavButton
        label="잠입"
        onClick={() => {
          navigate("/entry");
        }}
        isActive={activeTab === "잠입"}
      />
      <NavButton
        label="홈"
        onClick={() => handleNavClick("홈")}
        isActive={activeTab === "홈"}
      />
      <NavButton
        label="사무실"
        onClick={() => handleNavClick("사무실")}
        isActive={activeTab === "사무실"}
      />
      <NavButton
        label="기록"
        onClick={() => {
          handleNavClick("기록"); // 토스트
          navigate("/report"); // 페이지 이동
        }}
        isActive={activeTab === "기록"}
      />
    </div>
  );
};

export default Navbar;
