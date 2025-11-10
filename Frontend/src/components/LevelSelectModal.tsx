import React from "react";
import { Modal } from "./Modal";
import { fetchStartEntry } from "@/shared/api/entry";

type LevelSelectModalProps = {
  entryName: string;
  entryId: number;
  onClose: () => void;
};

export const LevelSelectModal = ({
  entryName,
  entryId,
  onClose,
}: LevelSelectModalProps) => {
  /** ✅ 난이도 클릭 이벤트 */
  const handleSelectLevel = async (level: "LOW" | "MID" | "HIGH") => {
    console.log(`🎮 난이도 선택: ${level}`);

    try {
      // ✅ 1️⃣ 서버로 세션 생성 요청
      const data = await fetchStartEntry(entryId);
      console.log("✅ 세션 생성 성공:", data);

      // ✅ 2️⃣ Android WebView 브릿지 호출 (러닝 시작)
      if (window.Android?.startRunning) {
        window.Android.startRunning();
        console.log("📲 AndroidBridge.startRunning() 호출됨");
      }

      // ✅ 나중에 서버 응답 데이터 활용 가능 (예: 세션 ID 저장 등)

      onClose(); // 모달 닫기
    } catch (err) {
      console.error("❌ 세션 생성 실패:", err);
      alert("서버에 세션을 생성하지 못했습니다.");
    }
  };

  return (
    <Modal title={entryName} onClose={onClose}>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: "12px",
          alignItems: "center",
          justifyContent: "center",
          padding: "10px",
        }}
      >
        <p style={{ fontSize: "14px", color: "#4b5563", marginBottom: "8px" }}>
          난이도를 선택하여 잠입을 시작하세요 👇
        </p>

        {/* 난이도 버튼 */}
        <button
          onClick={() => handleSelectLevel("LOW")}
          style={{
            width: "100%",
            padding: "12px",
            borderRadius: "8px",
            backgroundColor: "#86efac",
            border: "none",
            color: "#065f46",
            fontWeight: "bold",
            cursor: "pointer",
          }}
        >
          🔹 난이도 하(근처 정찰)
          <br />
          크레딧 +5
        </button>

        {/* <button
          onClick={() => handleSelectLevel("MID")}
          style={{
            width: "100%",
            padding: "12px",
            borderRadius: "8px",
            backgroundColor: "#60a5fa",
            border: "none",
            color: "white",
            fontWeight: "bold",
            cursor: "pointer",
          }}
        >
          🔸 난이도 중(일반 잠입)
          <br /> 크레딧 +10
        </button>

        <button
          onClick={() => handleSelectLevel("HIGH")}
          style={{
            width: "100%",
            padding: "12px",
            borderRadius: "8px",
            backgroundColor: "#f87171",
            border: "none",
            color: "white",
            fontWeight: "bold",
            cursor: "pointer",
          }}
        >
          🔺 난이도 상(전면 교전)
          <br />
          크레딧 +20
        </button> */}
      </div>
    </Modal>
  );
};
