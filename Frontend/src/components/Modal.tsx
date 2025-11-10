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
          width: "90%",
          maxWidth: "400px",
          backgroundColor: "white",
          borderRadius: "10px",
          padding: "20px",
          boxShadow: "0 4px 10px rgba(0,0,0,0.3)",
        }}
      >
        {title && (
          <h2
            style={{
              marginTop: 0,
              marginBottom: "10px",
              borderBottom: "1px solid #e5e7eb",
              paddingBottom: "5px",
            }}
          >
            {title}
          </h2>
        )}
        {children}
        <button
          onClick={onClose}
          style={{
            marginTop: "12px",
            backgroundColor: "#3b82f6",
            color: "white",
            padding: "8px 12px",
            borderRadius: "6px",
            border: "none",
            width: "100%",
          }}
        >
          닫기
        </button>
      </div>
    </div>
  );
};
