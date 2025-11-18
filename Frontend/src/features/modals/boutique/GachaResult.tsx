// src/features/modals/boutique/GachaResult.tsx
import { useState, useEffect } from "react";
import { ModalProps } from "@/app/modal/types";
import { Modal } from "@/shared/ui";
import { CommonButton } from "@/shared/ui";
import type { GachaResponse } from "@/entities/boutique/model/types";
import { SpritePreview } from "@/entities/boutique/ui/SpritePreview";
import { GiftIcon, StarIcon } from "@/shared/assets/icons";

type GachaResultPayload = {
  result: GachaResponse;
};

export default function BoutiqueGachaResult({ onClose, payload }: ModalProps) {
  const data = (payload as GachaResultPayload | undefined) ?? undefined;
  const result = data?.result;
  const [isAnimating, setIsAnimating] = useState(true);
  const [capsuleOpened, setCapsuleOpened] = useState(false);

  if (!result) {
    return (
      <Modal open onClose={onClose} title="오류">
        <p className="text-accent-red">가챠 결과를 불러올 수 없습니다.</p>
      </Modal>
    );
  }

  // 캡슐 애니메이션 타이밍
  useEffect(() => {
    // 1초 후 캡슐 열림
    const openTimer = setTimeout(() => {
      setCapsuleOpened(true);
    }, 1000);

    // 2초 후 결과 화면으로 전환
    const finishTimer = setTimeout(() => {
      setIsAnimating(false);
    }, 2000);

    return () => {
      clearTimeout(openTimer);
      clearTimeout(finishTimer);
    };
  }, []);

  // 희귀도별 색상
  const rarityColors = {
    common: "text-custom-gray",
    rare: "text-blue-400",
    epic: "text-purple-400",
    legendary: "text-orange-400",
  };

  const rarityLabels = {
    common: "일반",
    rare: "희귀",
    epic: "에픽",
    legendary: "전설",
  };

  // 다시 뽑기 핸들러
  const handleRetry = () => {
    // 커스텀 이벤트 발생 - boutique 페이지에서 10연뽑기 재실행
    window.dispatchEvent(new CustomEvent("boutique:retry-multi-gacha"));
    onClose();
  };

  return (
    <Modal
      open
      onClose={onClose}
      title={
        isAnimating ? "가챠 뽑는 중..." : `${result.drawType === "single" ? "1" : "10"}뽑 가챠 결과`
      }
      footer={
        !isAnimating ? (
          result.drawType === "multi" ? (
            <div className="flex gap-2 w-full">
              <CommonButton variant="outline" onClick={onClose} className="flex-1">
                확인
              </CommonButton>
              <CommonButton variant="solid" onClick={handleRetry} className="flex-1">
                다시 뽑기
              </CommonButton>
            </div>
          ) : (
            <CommonButton variant="solid" onClick={onClose} className="w-full">
              확인
            </CommonButton>
          )
        ) : undefined
      }
    >
      {isAnimating ? (
        // CSS 캡슐 애니메이션
        <div className="space-y-4">
          <div className="py-8 text-center">
            <p className="text-custom-white mb-6 flex items-center justify-center gap-2 text-lg font-bold">
              <GiftIcon className="size-5" />
              {result.drawType === "single" ? "가챠 뽑는 중..." : "10연차 뽑는 중..."}
            </p>

            {/* 캡슐 애니메이션 */}
            <div className="relative mx-auto flex h-64 w-64 items-center justify-center">
              {/* 외곽 글로우 효과 - 어두운 네이비 블루 */}
              <div
                className={`absolute inset-0 rounded-full blur-3xl transition-all duration-700 ${
                  capsuleOpened ? "scale-150 opacity-70" : "scale-100 opacity-40"
                }`}
                style={{
                  background:
                    "radial-gradient(circle, rgba(30, 58, 138, 0.6), rgba(15, 23, 42, 0.3), transparent)",
                }}
              />

              {/* 캡슐 컨테이너 - 픽셀 아트 스타일 */}
              <div
                className={`relative h-48 w-36 ${!capsuleOpened ? "animate-shake-lr" : "animate-pop"}`}
              >
                {/* 캡슐 상단 (밝은 시안) - 픽셀 계단식 */}
                <div
                  className={`absolute top-0 left-0 h-24 w-full transition-all duration-700 ease-out ${
                    capsuleOpened
                      ? "-translate-y-20 scale-110 -rotate-12 opacity-0"
                      : "translate-y-0 opacity-100"
                  }`}
                  style={{
                    background: `
                      linear-gradient(180deg, 
                        #FFFFFF 0%, #FFFFFF 5%,
                        #00E5FF 5%, #00E5FF 15%,
                        #00D4FF 15%, #00D4FF 25%,
                        #00C4E6 25%, #00C4E6 40%,
                        #00B0D0 40%, #00B0D0 55%,
                        #0098B8 55%, #0098B8 70%,
                        #0080A0 70%, #0080A0 85%,
                        #006888 85%, #006888 100%
                      )
                    `,
                    border: "4px solid #00B8E6",
                    borderBottom: "2px solid #0080A0",
                    boxShadow: `
                      0 0 0 1px #FFFFFF,
                      0 0 25px rgba(0, 229, 255, 0.9),
                      0 4px 0 #006080,
                      inset -6px -6px 0 rgba(0, 100, 130, 0.6),
                      inset 4px 4px 0 rgba(255, 255, 255, 0.5)
                    `,
                    imageRendering: "pixelated",
                    clipPath: `polygon(
                      20% 0%, 80% 0%,
                      85% 2%, 88% 4%, 90% 6%, 92% 8%, 94% 12%, 96% 16%,
                      97% 20%, 98% 26%, 99% 32%, 100% 40%,
                      100% 100%, 0% 100%,
                      0% 40%, 1% 32%, 2% 26%, 3% 20%,
                      4% 16%, 6% 12%, 8% 8%, 10% 6%, 12% 4%, 15% 2%
                    )`,
                  }}
                >
                  {/* 픽셀 하이라이트 블록 - 왼쪽 상단 */}
                  <div
                    className="absolute"
                    style={{
                      background: "#FFFFFF",
                      top: "12%",
                      left: "22%",
                      width: "28%",
                      height: "20%",
                      clipPath:
                        "polygon(8% 0%, 92% 0%, 100% 15%, 100% 85%, 92% 100%, 8% 100%, 0% 85%, 0% 15%)",
                      opacity: 0.7,
                      imageRendering: "pixelated",
                    }}
                  />

                  {/* 픽셀 하이라이트 블록 - 작은 블록 */}
                  <div
                    className="absolute"
                    style={{
                      background: "#00E5FF",
                      top: "35%",
                      left: "18%",
                      width: "12%",
                      height: "15%",
                      clipPath:
                        "polygon(10% 0%, 90% 0%, 100% 20%, 100% 80%, 90% 100%, 10% 100%, 0% 80%, 0% 20%)",
                      opacity: 0.6,
                      imageRendering: "pixelated",
                    }}
                  />

                  {/* 픽셀 그림자 블록 - 오른쪽 */}
                  <div
                    className="absolute"
                    style={{
                      background: "#005070",
                      top: "30%",
                      right: "15%",
                      width: "18%",
                      height: "25%",
                      clipPath:
                        "polygon(10% 0%, 90% 0%, 100% 15%, 100% 85%, 90% 100%, 10% 100%, 0% 85%, 0% 15%)",
                      opacity: 0.5,
                      imageRendering: "pixelated",
                    }}
                  />
                </div>

                {/* 캡슐 하단 (진한 네이비) - 픽셀 계단식 */}
                <div
                  className={`absolute bottom-0 left-0 h-24 w-full transition-all duration-700 ease-out ${
                    capsuleOpened
                      ? "translate-y-20 scale-110 rotate-12 opacity-0"
                      : "translate-y-0 opacity-100"
                  }`}
                  style={{
                    background: `
                      linear-gradient(0deg, 
                        #001A40 0%, #001A40 15%,
                        #0A2858 15%, #0A2858 30%,
                        #1E3A8A 30%, #1E3A8A 45%,
                        #2563EB 45%, #2563EB 60%,
                        #3B82F6 60%, #3B82F6 75%,
                        #60A5FA 75%, #60A5FA 90%,
                        #93C5FD 90%, #93C5FD 95%,
                        #DBEAFE 95%, #DBEAFE 100%
                      )
                    `,
                    border: "4px solid #1E40AF",
                    borderTop: "2px solid #60A5FA",
                    boxShadow: `
                      0 0 0 1px #001A40,
                      0 0 25px rgba(30, 64, 175, 0.9),
                      0 -4px 0 #001A40,
                      inset -6px 6px 0 rgba(10, 30, 80, 0.7),
                      inset 4px -4px 0 rgba(147, 197, 253, 0.4)
                    `,
                    imageRendering: "pixelated",
                    clipPath: `polygon(
                      0% 0%, 100% 0%,
                      100% 60%, 99% 68%, 98% 74%, 97% 80%,
                      96% 84%, 94% 88%, 92% 92%, 90% 94%, 88% 96%, 85% 98%,
                      80% 100%, 20% 100%,
                      15% 98%, 12% 96%, 10% 94%, 8% 92%, 6% 88%, 4% 84%,
                      3% 80%, 2% 74%, 1% 68%, 0% 60%
                    )`,
                  }}
                >
                  {/* 픽셀 하이라이트 블록 - 하단 중앙 */}
                  <div
                    className="absolute"
                    style={{
                      background: "#93C5FD",
                      bottom: "18%",
                      left: "25%",
                      width: "30%",
                      height: "22%",
                      clipPath:
                        "polygon(8% 0%, 92% 0%, 100% 15%, 100% 85%, 92% 100%, 8% 100%, 0% 85%, 0% 15%)",
                      opacity: 0.7,
                      imageRendering: "pixelated",
                    }}
                  />

                  {/* 픽셀 하이라이트 블록 - 작은 블록 */}
                  <div
                    className="absolute"
                    style={{
                      background: "#60A5FA",
                      bottom: "45%",
                      left: "16%",
                      width: "14%",
                      height: "18%",
                      clipPath:
                        "polygon(10% 0%, 90% 0%, 100% 20%, 100% 80%, 90% 100%, 10% 100%, 0% 80%, 0% 20%)",
                      opacity: 0.6,
                      imageRendering: "pixelated",
                    }}
                  />

                  {/* 픽셀 그림자 블록 - 오른쪽 하단 */}
                  <div
                    className="absolute"
                    style={{
                      background: "#001A40",
                      bottom: "28%",
                      right: "18%",
                      width: "20%",
                      height: "28%",
                      clipPath:
                        "polygon(10% 0%, 90% 0%, 100% 15%, 100% 85%, 90% 100%, 10% 100%, 0% 85%, 0% 15%)",
                      opacity: 0.6,
                      imageRendering: "pixelated",
                    }}
                  />
                </div>

                {/* 중앙 다이아몬드 빛 효과 (캡슐이 열릴 때) - 픽셀 아트 스타일 (어두운 네이비 블루) */}
                {capsuleOpened && (
                  <div className="absolute inset-0 flex items-center justify-center">
                    {/* 최외곽 픽셀 글로우 - 어두운 네이비 */}
                    <div
                      className="h-28 w-28 animate-ping"
                      style={{
                        background: "#1E3A8A",
                        transform: "rotate(45deg)",
                        imageRendering: "pixelated",
                        opacity: 0.7,
                        clipPath:
                          "polygon(20% 10%, 80% 10%, 90% 20%, 90% 80%, 80% 90%, 20% 90%, 10% 80%, 10% 20%)",
                      }}
                    />

                    {/* 외부 다이아몬드 레이어 1 - 픽셀 블록 (네이비 블루) */}
                    <div
                      className="absolute h-24 w-24"
                      style={{
                        background: `
                          linear-gradient(135deg, 
                            #0F172A 0%, #0F172A 20%,
                            #1E293B 20%, #1E293B 40%,
                            #1E3A8A 40%, #1E3A8A 60%,
                            #1E293B 60%, #1E293B 80%,
                            #0F172A 80%, #0F172A 100%
                          )
                        `,
                        transform: "rotate(45deg)",
                        border: "4px solid #1E3A8A",
                        boxShadow: `
                          0 0 0 2px #334155,
                          0 0 30px rgba(30, 58, 138, 0.9),
                          0 0 0 #000000,
                          inset -6px -6px 0 rgba(15, 23, 42, 0.9),
                          inset 4px 4px 0 rgba(71, 85, 105, 0.6)
                        `,
                        imageRendering: "pixelated",
                        clipPath:
                          "polygon(20% 8%, 80% 8%, 88% 16%, 92% 20%, 92% 80%, 88% 84%, 80% 92%, 20% 92%, 12% 84%, 8% 80%, 8% 20%, 12% 16%)",
                      }}
                    />

                    {/* 중간 다이아몬드 레이어 2 - 픽셀 계단 (어두운 네이비) */}
                    <div
                      className="absolute h-18 w-18"
                      style={{
                        background: `
                          linear-gradient(135deg, 
                            #475569 0%, #475569 12%,
                            #2563EB 12%, #2563EB 25%,
                            #1E3A8A 25%, #1E3A8A 38%,
                            #1E293B 38%, #1E293B 50%,
                            #0F172A 50%, #0F172A 62%,
                            #1E293B 62%, #1E293B 75%,
                            #1E3A8A 75%, #1E3A8A 88%,
                            #2563EB 88%, #2563EB 100%
                          )
                        `,
                        transform: "rotate(45deg)",
                        border: "3px solid #334155",
                        boxShadow: `
                          0 0 0 1px #1E3A8A,
                          0 0 35px rgba(30, 58, 138, 1),
                          0 0 50px rgba(37, 99, 235, 0.6),
                          inset -4px -4px 0 rgba(15, 23, 42, 1),
                          inset 3px 3px 0 rgba(100, 116, 139, 0.8)
                        `,
                        imageRendering: "pixelated",
                        clipPath:
                          "polygon(22% 12%, 78% 12%, 84% 18%, 88% 22%, 88% 78%, 84% 82%, 78% 88%, 22% 88%, 18% 82%, 12% 78%, 12% 22%, 18% 18%)",
                        width: "72px",
                        height: "72px",
                      }}
                    >
                      {/* 픽셀 하이라이트 블록 - 왼쪽 상단 (밝은 블루) */}
                      <div
                        className="absolute"
                        style={{
                          background: "#475569",
                          top: "18%",
                          left: "18%",
                          width: "32%",
                          height: "28%",
                          clipPath:
                            "polygon(12% 0%, 88% 0%, 100% 20%, 100% 80%, 88% 100%, 12% 100%, 0% 80%, 0% 20%)",
                          imageRendering: "pixelated",
                          opacity: 0.9,
                        }}
                      />
                      {/* 픽셀 그림자 블록 - 오른쪽 하단 (거의 검은색) */}
                      <div
                        className="absolute"
                        style={{
                          background: "#020617",
                          bottom: "18%",
                          right: "18%",
                          width: "38%",
                          height: "35%",
                          clipPath:
                            "polygon(12% 0%, 88% 0%, 100% 20%, 100% 80%, 88% 100%, 12% 100%, 0% 80%, 0% 20%)",
                          imageRendering: "pixelated",
                          opacity: 0.85,
                        }}
                      />
                    </div>

                    {/* 내부 코어 다이아몬드 - 회전 */}
                    <div
                      className="absolute h-14 w-14 animate-spin"
                      style={{
                        background: `
                          linear-gradient(135deg, 
                            #FFFFFF 0%, #00E5FF 30%, #00D4FF 50%, 
                            #00C4E6 70%, #FFFFFF 100%
                          )
                        `,
                        transform: "rotate(45deg)",
                        animationDuration: "3s",
                        border: "2px solid #FFFFFF",
                        imageRendering: "pixelated",
                        boxShadow: `
                          0 0 30px rgba(0, 229, 255, 1), 
                          0 0 45px rgba(255, 255, 255, 0.8), 
                          inset -3px -3px 6px rgba(0, 150, 180, 0.7), 
                          inset 3px 3px 6px rgba(255, 255, 255, 0.7)
                        `,
                        clipPath: `polygon(
                          25% 12%, 75% 12%, 82% 18%, 88% 25%, 
                          88% 75%, 82% 82%, 75% 88%, 25% 88%, 
                          18% 82%, 12% 75%, 12% 25%, 18% 18%
                        )`,
                      }}
                    >
                      {/* 중심 하이라이트 */}
                      <div
                        className="absolute"
                        style={{
                          background:
                            "radial-gradient(circle, rgba(255, 255, 255, 1), rgba(0, 229, 255, 0.5))",
                          top: "28%",
                          left: "28%",
                          width: "30%",
                          height: "30%",
                          imageRendering: "pixelated",
                        }}
                      />
                    </div>
                  </div>
                )}
              </div>
            </div>

            <p className="text-custom-gray mt-4 text-sm">
              {capsuleOpened ? (
                <span className="flex items-center justify-center gap-2">
                  <StarIcon className="size-4" />
                  <span>결과 확인 중...</span>
                </span>
              ) : (
                "잠시만 기다려주세요..."
              )}
            </p>
          </div>

          {/* 애니메이션 스타일 */}
          <style>{`
            @keyframes shake-lr {
              0%, 100% { transform: translateX(0) rotate(0deg); }
              10% { transform: translateX(-8px) rotate(-3deg); }
              20% { transform: translateX(8px) rotate(3deg); }
              30% { transform: translateX(-6px) rotate(-2deg); }
              40% { transform: translateX(6px) rotate(2deg); }
              50% { transform: translateX(-8px) rotate(-3deg); }
              60% { transform: translateX(8px) rotate(3deg); }
              70% { transform: translateX(-4px) rotate(-1deg); }
              80% { transform: translateX(4px) rotate(1deg); }
              90% { transform: translateX(-2px) rotate(-0.5deg); }
            }
            
            @keyframes pop {
              0% { transform: scale(1); }
              50% { transform: scale(1.15); }
              100% { transform: scale(1); }
            }
            
            .animate-shake-lr {
              animation: shake-lr 0.6s ease-in-out infinite;
            }
            
            .animate-pop {
              animation: pop 0.3s ease-out;
            }
          `}</style>
        </div>
      ) : (
        // 결과 화면
        <div className="flex h-full min-h-0 flex-col space-y-4">
          {/* 가챠 정보 */}
          <div className="bg-section-bg border-primary/20 rounded-lg border p-4">
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div>
                <span className="text-custom-gray">뽑은 개수:</span>
                <span className="text-custom-white ml-2 font-medium">{result.totalDraws}개</span>
              </div>
              <div>
                <span className="text-custom-gray">소비 CR:</span>
                <span className="text-accent-red ml-2 font-medium">-{result.spentCredit}</span>
              </div>
              <div className="col-span-2">
                <span className="text-custom-gray">남은 CR:</span>
                <span className="text-primary ml-2 font-bold">
                  {result.remainingCredit.toLocaleString()}
                </span>
              </div>
            </div>
          </div>

          {/* 획득 아이템 목록 */}
          <div className="flex min-h-0 flex-1 flex-col space-y-2">
            <h3 className="text-custom-white text-sm font-bold">획득 아이템</h3>
            <div className="grid min-h-0 flex-1 grid-cols-2 gap-2 overflow-y-auto pr-1">
              {result.items.map((item, index) => (
                <div
                  key={`${item.itemId}-${index}`}
                  className="bg-section-bg border-primary/20 relative rounded-lg border p-2"
                >
                  {/* 신규 획득 뱃지 */}
                  {item.isNew && (
                    <div className="bg-primary text-custom-black absolute top-4 right-[14px] z-10 rounded-full px-2 py-0.5 text-[10px] font-bold">
                      NEW
                    </div>
                  )}

                  {/* 아이템 이미지 */}
                  <div className="bg-custom-black/50 mb-2 aspect-square overflow-hidden rounded">
                    <SpritePreview
                      basePath={item.path}
                      category={item.category}
                      className="h-full w-full"
                    />
                  </div>

                  {/* 아이템 정보 */}
                  <div className="text-center">
                    <p className="text-custom-white mb-1 truncate text-xs font-medium">
                      {item.itemName}
                    </p>
                    <p className={`text-xs font-bold ${rarityColors[item.rarity]}`}>
                      {rarityLabels[item.rarity]}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
}
