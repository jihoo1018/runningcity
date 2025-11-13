import { useState } from "react";

const ShowroomMePage = () => {
  const [tab, setTab] = useState<"me" | "friend" | "global">("me");

  return (
    <div className="flex min-h-screen flex-col">
      <h1>옷 갈아입히기</h1>
    </div>
  );
};

export default ShowroomMePage;
