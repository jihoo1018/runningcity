import { AuthUser, useAuthStore } from "./useAuthStore";

/**
 * @function updateUser
 * @description
 * Zustand 스토어에 저장된 사용자 정보(`AuthUser`) 중 일부 필드를 선택적으로 수정합니다.
 * 기존 상태를 보존하면서 전달된 `patch` 객체의 속성만 덮어씁니다.
 *
 * @param {Partial<AuthUser>} patch - 변경할 사용자 정보 필드만 포함한 객체
 *
 * @example
 * updateUser({ nickname: "새닉네임" });
 * updateUser({ email: "new@example.com", totalExp: 1200 });
 */
export function updateUser(patch: Partial<AuthUser>) {
  const { user } = useAuthStore.getState();
  if (!user) return;
  const next: AuthUser = { ...user, ...patch };

  let changed = false;
  (Object.keys(patch) as (keyof AuthUser)[]).forEach((key) => {
    if (user[key] !== next[key]) {
      changed = true;
    }
  });
  if (!changed) return;

  useAuthStore.setState({ user: next });
}

/**
 * @function updateUserWith
 * @description
 * 현재 사용자 상태(`AuthUser`)를 기반으로 계산된 결과를 이용해 갱신합니다.
 * 누적 경험치 증가 등 현재 상태를 참조해야 하는 경우에 사용합니다.
 *
 * @param {(cur: AuthUser) => Partial<AuthUser>} fn - 현재 상태를 입력받아 변경할 필드만 반환하는 함수
 *
 * @example
 * updateUserWith((cur) => ({ totalExp: (cur.totalExp ?? 0) + 100 }));
 */
export function updateUserWith(fn: (cur: AuthUser) => Partial<AuthUser>) {
  const { user } = useAuthStore.getState();
  if (!user) return;
  const patch = fn(user);
  return updateUser(patch);
}

/**
 * @function signOut
 * @description
 * 사용자 인증 정보를 초기화합니다.
 * 로그인 상태를 해제하거나 계정을 삭제할 때 사용합니다.
 *
 * @example
 * signOut(); // Zustand 스토어에서 user를 null로 초기화
 */
export function signOut() {
  useAuthStore.getState().clear();
}
