import { useState } from "react";

const ShowroomEditPage = () => {
  const [tab, setTab] = useState<"me" | "friend" | "global">("me");

  return (
    <div className="flex min-h-screen flex-col">
      <h1>사무실 정보 수정</h1>
    </div>
  );
};

export default ShowroomEditPage;
