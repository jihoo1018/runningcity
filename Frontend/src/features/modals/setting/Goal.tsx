import { ModalProps } from "@/app/modal/types";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { CommonButton, Modal } from "@/shared/ui";
import { FormEvent, useMemo, useState } from "react";

export default function SettingGoal({ onClose }: ModalProps) {
  const { to, back } = useModalRouter();
  const [target, setTarget] = useState<number>(1); // TODO: 기본값 유저 설정으로 변경

  const MIN = 1;
  const MAX = 40;
  const STEP = 1;

  const pct = useMemo(() => ((target - MIN) / (MAX - MIN)) * 100, [target]);

  const handleInputTarget: React.ChangeEventHandler<HTMLInputElement> = (e) => {
    const v = Math.min(MAX, Math.max(MIN, e.currentTarget.valueAsNumber));
    const rounded = Math.round(v);
    setTarget(rounded);
  };

  const onSubmit = (e: FormEvent) => {
    e.preventDefault();
    // TODO: api 요청
    // 성공/실패에 따른 모달 안내
    to("setting", "confirm", { target });
  };

  return (
    <Modal open onClose={onClose} title="목표 수정" onBack={back}>
      <form className="m-0 flex flex-col gap-8" onSubmit={onSubmit}>
        <div className="flex flex-col gap-2">
          <div className="flex items-center justify-between px-1">
            <p className="text-custom-gray text-sm">나의 목표 거리</p>
            <div className="flex items-baseline justify-center gap-1">
              <strong className="text-xl font-semibold tabular-nums">{target}</strong>
              <span className="text-custom-gray text-sm">km</span>
            </div>
          </div>
          <div
            className="p-[6px]"
            style={{
              // border: primary/50
              border: "2px solid color-mix(in srgb, var(--color-primary), transparent 50%)",
            }}
          >
            <input
              type="range"
              name="targetDistanceKm"
              placeholder="1 ~ 40"
              min={MIN}
              max={MAX}
              step={STEP}
              value={target}
              onChange={handleInputTarget}
              style={
                {
                  "--pct": `${pct}%`,
                } as React.CSSProperties
              }
              className="rc-range block w-full appearance-none focus:outline-none"
              aria-label="목표 거리"
            />
          </div>
        </div>
        <CommonButton variant="solid" type="submit">
          저장하기
        </CommonButton>
      </form>
      <style>{`
        .rc-range {
          height: 14px;
          background:
            /* 선택 구간 타일 */
            repeating-linear-gradient(
              90deg,
              var(--color-primary) 0 10px,
              transparent 10px 12px
            ) left / var(--pct) 100% no-repeat,

            /* 미선택 구간 타일 */
            repeating-linear-gradient(
              90deg,
              color-mix(in srgb, var(--color-custom-gray), transparent 70%) 0 10px,
              transparent 10px 12px
            ) left / 100% 100% no-repeat,

            /* 트랙 베이스 */
            color-mix(in srgb, var(--color-custom-gray), transparent 80%);
        }

        /* WebKit */
        .rc-range::-webkit-slider-runnable-track {
          height: 18px;
          background: transparent;
          border-radius: 4px;
        }
        .rc-range::-webkit-slider-thumb {
          -webkit-appearance: none;
          height: 18px;
          width: 12px;
          margin-top: 0;
          border-radius: 2px;
          background: var(--color-primary);
          border: 1px solid color-mix(in srgb, var(--color-primary), black 20%);
          box-shadow:
            0 0 0 2px color-mix(in srgb, var(--color-primary), transparent 60%),
            0 0 0 4px color-mix(in srgb, var(--color-primary), transparent 85%); 
        }

        /* Firefox */
        .rc-range::-moz-range-track {
          height: 18px;
          background: transparent;
          border: none;
        }
        .rc-range::-moz-range-progress {
          height: 18px;
          background: transparent; 
        }
        .rc-range::-moz-range-thumb {
          height: 18px;
          width: 12px;
          border-radius: 2px;
          background: var(--color-primary);
          border: 1px solid color-mix(in srgb, var(--color-primary), black 20%);
        }

        /* Edge/Chromium(레거시) */
        .rc-range::-ms-fill-lower { background: transparent; }
        .rc-range::-ms-fill-upper { background: transparent; }
      `}</style>
    </Modal>
  );
}
