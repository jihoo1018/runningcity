// src/shared/ui/NavButton.tsx

interface NavButtonProps {
    label: string;
    onClick: () => void;
    isActive?: boolean;
}

const NavButton = ({ label, onClick, isActive = false }: NavButtonProps) => {
    return (
        <button
            onClick={onClick}
            style={{
                padding: 'clamp(6px, 2vw, 10px) clamp(2px, 1vw, 6px)', // 반응형 패딩
                fontSize: 'clamp(10px, 2.8vw, 14px)', // 반응형 폰트 크기
                fontWeight: isActive ? 'bold' : 'normal',
                color: isActive ? '#3b82f6' : '#6b7280',
                backgroundColor: 'transparent',
                border: 'none',
                cursor: 'pointer',
                transition: 'color 0.2s',
                minWidth: '44px',
                minHeight: '44px',
                flex: 1,
                WebkitTapHighlightColor: 'transparent', // 터치 하이라이트 제거
                touchAction: 'manipulation', // 터치 최적화
                userSelect: 'none', // 텍스트 선택 방지
            }}
        >
            {label}
        </button>
    );
};

export default NavButton;
