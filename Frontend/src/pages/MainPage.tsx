// src/pages/MainPage.tsx

import { useState, useEffect } from 'react';
import Button from '../components/Button';
import { AndroidBridge } from '../utils/webview';

const MainPage = () => {
    const [gpsData, setGpsData] = useState<{ lat: number; lng: number } | null>(null);

    useEffect(() => {
        if (AndroidBridge.isAndroid()) {
            console.log('Android WebView 환경');
        } else {
            console.log('브라우저 환경 (개발 모드)');
        }
    }, []);

    const handleRunning = () => {
        AndroidBridge.startRunning();
        AndroidBridge.showToast('러닝을 시작합니다! 🏃');
        const gps = AndroidBridge.getGPSData();
        setGpsData(gps);
    };

    const handleSettings = () => {
        AndroidBridge.showToast('설정');
    };

    const handleFriends = () => {
        AndroidBridge.showToast('친구');
    };

    const handleNotifications = () => {
        AndroidBridge.showToast('알림');
    };

    const handleQuest = () => {
        AndroidBridge.showToast('퀘스트');
    };

    return (
        <div style={{
            width: '100vw',
            height: '100vh',
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: '#f3f4f6',
        }}>
            {/* 헤더 영역 - 높이 고정 */}
            <div style={{
                padding: '12px 16px',
                display: 'flex',
                justifyContent: 'flex-end', // ✅ 우측 정렬로 변경
                alignItems: 'flex-start',
                flexShrink: 0,
            }}>
                {/* 우측 상단: 세로로 정렬된 버튼들 */}
                <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '8px'
                }}>
                    <Button label="설정" onClick={handleSettings} variant="circle" />
                    <Button label="친구" onClick={handleFriends} variant="circle" />
                    <Button label="알림" onClick={handleNotifications} variant="circle" />
                    <Button label="퀘스트" onClick={handleQuest} variant="circle" />
                </div>
            </div>

            {/* 메인 콘텐츠 영역 - 남은 공간 차지 */}
            <div style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                padding: '20px',
                overflow: 'auto',
            }}>
                {/* 캐릭터 영역 */}
                <div style={{
                    width: '160px',
                    height: '240px',
                    backgroundColor: '#e5e7eb',
                    borderRadius: '16px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    marginBottom: '20px',
                    boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
                }}>
                    <p style={{
                        color: '#6b7280',
                        fontSize: '14px',
                        textAlign: 'center',
                        margin: 0,
                    }}>
                        캐릭터<br/>이미지
                    </p>
                </div>

                {/* 러닝 버튼 */}
                <button
                    onClick={handleRunning}
                    style={{
                        padding: '14px 28px',
                        fontSize: '16px',
                        fontWeight: 'bold',
                        backgroundColor: '#3b82f6',
                        color: 'white',
                        border: 'none',
                        borderRadius: '12px',
                        cursor: 'pointer',
                        minHeight: '48px',
                        boxShadow: '0 2px 8px rgba(59, 130, 246, 0.3)',
                    }}
                >
                    러닝 에너지 모으러 가기
                </button>

                {/* 디버그 정보 (개발용) */}
                {gpsData && (
                    <div style={{
                        marginTop: '12px',
                        fontSize: '11px',
                        color: '#9ca3af',
                        textAlign: 'center',
                    }}>
                        <p style={{ margin: 0 }}>
                            GPS: {gpsData.lat.toFixed(4)}, {gpsData.lng.toFixed(4)}
                        </p>
                    </div>
                )}
            </div>

            {/* 하단 네비게이션 바 */}
            <div style={{
                backgroundColor: 'white',
                borderTop: '1px solid #e5e7eb',
                padding: '8px 0',
                display: 'flex',
                justifyContent: 'space-around',
                alignItems: 'center',
                boxShadow: '0 -2px 10px rgba(0,0,0,0.05)',
                flexShrink: 0,
            }}>
                <NavButton label="부티크" onClick={() => AndroidBridge.showToast('부티크')} />
                <NavButton label="해킹" onClick={() => AndroidBridge.showToast('해킹')} />
                <NavButton label="홈" onClick={() => AndroidBridge.showToast('홈')} isActive />
                <NavButton label="사무실" onClick={() => AndroidBridge.showToast('사무실')} />
                <NavButton label="기록" onClick={() => AndroidBridge.showToast('기록')} />
            </div>
        </div>
    );
};

// 하단 네비게이션 버튼 컴포넌트
const NavButton = ({
                       label,
                       onClick,
                       isActive = false
                   }: {
    label: string;
    onClick: () => void;
    isActive?: boolean;
}) => {
    return (
        <button
            onClick={onClick}
            style={{
                padding: '8px 4px',
                fontSize: '12px',
                fontWeight: isActive ? 'bold' : 'normal',
                color: isActive ? '#3b82f6' : '#6b7280',
                backgroundColor: 'transparent',
                border: 'none',
                cursor: 'pointer',
                transition: 'color 0.2s',
                minWidth: '44px',
                minHeight: '44px',
                flex: 1,
            }}
        >
            {label}
        </button>
    );
};

export default MainPage;