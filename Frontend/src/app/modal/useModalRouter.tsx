import { useLocation, useNavigate } from "react-router-dom";
import type { FlowKey, StepKey } from "./types";

type StackItem = { flow: FlowKey; step: StepKey<any> };

export function useModalRouter() {
  const navigate = useNavigate();
  const location = useLocation();

  const background = (location.state as any)?.background ?? location;
  const prevStack: StackItem[] = (location.state as any)?.stack ?? [];
  const prevPayload = (location.state as any)?.payload;

  // 최초 오픈: 배경 + 초기 스택으로 /m/flow/step 진입(push)
  const open = <F extends FlowKey>(flow: F, step: StepKey<F>, payload?: unknown) => {
    navigate(`/m/${flow}/${step}`, {
      state: {
        background: location, // 현재 페이지를 배경으로 고정
        stack: [{ flow, step }], // 초기 스택
        payload, // 넘겨받은 데이터
      },
    });
  };

  // 다음 단계로 (replace) → forward 히스토리 생성 안 함
  const to = <F extends FlowKey>(flow: F, step: StepKey<F>, payload?: unknown) => {
    const nextStack = [...prevStack, { flow, step }];

    navigate(`/m/${flow}/${step}`, {
      replace: true,
      state: {
        background,
        stack: nextStack,
        payload: payload ?? prevPayload, // 없다면 이전 payload 유지
      },
    });
  };

  // 이전 단계로 (replace), forward 불가
  const back = () => {
    if (prevStack.length <= 1) {
      // 이전 단계가 없으면 닫기 동작
      navigate(background, { replace: true });
      return;
    }
    const popped = prevStack.slice(0, -1);
    const prev = popped[popped.length - 1];

    navigate(`/m/${prev.flow}/${prev.step}`, {
      replace: true,
      state: {
        background,
        stack: popped,
        payload: prevPayload,
      },
    });
  };

  // 닫기 (replace) → 밖에서 뒤/앞으로 모달 복귀 불가
  const close = () => navigate(background, { replace: true });

  return { open, to, back, close };
}
