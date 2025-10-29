// src/shared/ui/Button.tsx

interface ButtonProps {
    label: string;           // 버튼에 표시될 텍스트
    onClick?: () => void;    // 클릭 시 실행될 함수 (선택사항)
    variant?: 'primary' | 'secondary' | 'circle';  // 버튼 스타일 종류
    disabled?: boolean;      // 비활성화 여부 (선택사항)
}

const Button = ({ label, onClick, variant = 'primary', disabled = false }: ButtonProps) => {
    // 버튼 스타일을 variant에 따라 다르게 설정
    const getButtonStyle = () => {
        const baseStyle = {
            padding: '12px 24px',
            fontSize: '16px',
            border: 'none',
            cursor: disabled ? 'not-allowed' : 'pointer',
            opacity: disabled ? 0.5 : 1,
            transition: 'all 0.3s ease',
            WebkitTapHighlightColor: 'transparent', // 터치 하이라이트 제거
            touchAction: 'manipulation', // 터치 최적화
            userSelect: 'none' as const, // 텍스트 선택 방지
        };

        // variant에 따른 추가 스타일
        switch (variant) {
            case 'circle':
                return {
                    ...baseStyle,
                    width: '15vw', // 화면 너비의 15%
                    height: '15vw', // 정사각형 유지
                    maxWidth: '80px', // 최대 크기 제한
                    maxHeight: '80px',
                    minWidth: '60px', // 최소 크기 보장
                    minHeight: '60px',
                    borderRadius: '50%',
                    backgroundColor: '#d1d5db',
                    color: '#374151',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: 'clamp(10px, 2.5vw, 14px)', // 반응형 폰트 크기
                    padding: '0', // circle은 패딩 제거
                };
            case 'secondary':
                return {
                    ...baseStyle,
                    backgroundColor: '#6b7280',
                    color: 'white',
                    borderRadius: '8px',
                };
            case 'primary':
            default:
                return {
                    ...baseStyle,
                    backgroundColor: '#3b82f6',
                    color: 'white',
                    borderRadius: '8px',
                };
        }
    };

    return (
        <button
            onClick={onClick}
            disabled={disabled}
            style={getButtonStyle()}
        >
            {label}
        </button>
    );
};

export default Button;
