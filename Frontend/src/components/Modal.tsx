type ModalProps = {
  title?: string;
  onClose: () => void;
  children: React.ReactNode;
};

export const Modal = ({ title, onClose, children }: ModalProps) => {
  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        width: "100vw",
        height: "100vh",
        backgroundColor: "rgba(0, 0, 0, 0.5)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 9999,
      }}
      onClick={onClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          position: "relative", // ✅ X버튼 배치용
          width: "90%",
          maxWidth: "400px",
          backgroundColor: "#1B3240",
          border: "1px solid #E6FFFF",
          borderRadius: "10px",
          padding: "20px",
          boxShadow: "0 4px 10px rgba(0,0,0,0.3)",
        }}
      >
        {/* ✅ 닫기 X 버튼 */}
        <button
          onClick={onClose}
          style={{
            position: "absolute",
            top: "8px",
            right: "20px",
            background: "transparent",
            border: "none",
            color: "#E6FFFF",
            fontSize: "20px",
            cursor: "pointer",
            fontWeight: "bold",
          }}
          aria-label="Close"
        >
          ✕
        </button>

        {/* 제목 */}
        {title && (
          <h2
            style={{
              marginTop: 0,
              marginBottom: "10px",
              borderBottom: "1px solid #E6FFFF",
              paddingBottom: "5px",
              color: "#E6FFFF",
            }}
          >
            {title}
          </h2>
        )}

        {/* 내용 */}
        {children}
      </div>
    </div>
  );
};
