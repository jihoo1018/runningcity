// src/entites/entry/ui/LevelSelectModal.tsx

import React from "react";
import { useNavigate } from "react-router-dom";
import { Modal } from "../../../components/Modal";
import { fetchStartEntry } from "@/entities/entry/api";

type LevelSelectModalProps = {
  entryName: string;
  baseId: number;
  userId: number;
  onClose: () => void;
};

export const LevelSelectModal = ({ entryName, baseId, userId, onClose }: LevelSelectModalProps) => {
  const navigate = useNavigate();
  /** ✅ 난이도 클릭 이벤트 */
  const handleSelectLevel = async (level: "LOW" | "MID" | "HIGH") => {
    // console.log(`🎮 난이도 선택: ${level}`);

    try {
      // ✅ 1️⃣ 서버로 세션 생성 요청
      const data = await fetchStartEntry(userId, baseId);
      console.log("✅ 세션 생성 성공:", data);
      console.log("▶ sessionId:", data.sessionId);

      // ✅ 2️⃣ Android WebView 브릿지 호출 (러닝 시작)
      if (window.Android?.startRunning && data) {
        console.log("[WEB] Android.startRunning 호출 직전", data.sessionId, typeof data.sessionId);
        window.Android.startRunning(String(data.sessionId)); // 문자열로 넘기는 것도 안전
        console.log("[WEB] Android.startRunning 호출 완료");
      } else {
        console.log("[WEB] Android 객체 없음 or startRunning 없음", window.Android);
      }



      onClose(); // 모달 닫기
      navigate("/running", { state: { sessionId: data.sessionId } }); // TODO 달리기 화면 중간 잇기 -> 지금은 바로 러닝 화면으로 이동
    } catch (err) {
      console.error("❌ 세션 생성 실패:", err);
      // alert("서버에 세션을 생성하지 못했습니다.");
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
        <p style={{ fontSize: "12px", color: "#E6FFFF", marginBottom: "8px" }}>
          난이도를 선택하여 잠입을 시작하세요 👇
        </p>

        {/* 난이도 버튼 */}
        <button
          onClick={() => handleSelectLevel("LOW")}
          style={{
            width: "100%",
            padding: "12px",
            borderRadius: "8px",
            backgroundColor: "#00E6FF",
            border: "none",
            color: "#1D2330",
            fontWeight: "bold",
            cursor: "pointer",
          }}
        >
          🔹 난이도 하(근처 정찰)
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
        </button> */}
      </div>
    </Modal>
  );
};
