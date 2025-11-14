type AnchorPoint = {
  from: number;
  needExp: number;
  rewardCR?: number;
  bracket?: string;
};

export function geometricInterpolate(start: number, end: number, steps: number): number[] {
  // steps 개의 값(양 끝 제외 내부) 생성: start→...→end 사이
  if (steps <= 0) return [];
  if (start <= 0 || end <= 0) {
    // 0 이하가 섞이면 기하보간이 깨지므로 선형으로 폴백
    const delta = (end - start) / (steps + 1);
    return Array.from({ length: steps }, (_, i) => Math.round(start + delta * (i + 1)));
  }
  const ratio = Math.pow(end / start, 1 / (steps + 1));
  return Array.from({ length: steps }, (_, i) => Math.round(start * Math.pow(ratio, i + 1)));
}

export function linearInterpolateInt(start: number, end: number, steps: number): number[] {
  if (steps <= 0) return [];
  const delta = (end - start) / (steps + 1);
  return Array.from({ length: steps }, (_, i) => Math.round(start + delta * (i + 1)));
}

export function expandAnchors(anchors: AnchorPoint[], maxLevel: number): Required<AnchorPoint>[] {
  // 정렬 및 중복 제거 가드
  const list = [...anchors].sort((a, b) => a.from - b.from);

  const out: Required<AnchorPoint>[] = [];

  for (let i = 0; i < list.length; i++) {
    const cur = list[i];
    const next = list[i + 1];

    // 현재 앵커는 그대로 push
    out.push({
      from: cur.from,
      needExp: cur.needExp,
      rewardCR: cur.rewardCR ?? out.at(-1)?.rewardCR ?? 0,
      bracket: cur.bracket ?? out.at(-1)?.bracket ?? "",
    });

    if (!next) continue;

    // 중간 레벨 채우기: cur.from+1 … next.from-1
    const gap = next.from - cur.from - 1;
    if (gap <= 0) continue;

    // 필요 EXP: 기하 보간
    const needSeries = geometricInterpolate(cur.needExp, next.needExp, gap);

    // 보상 CR: 양쪽 값이 모두 있으면 선형 보간, 아니면 직전값 유지
    const hasCR = cur.rewardCR != null && next.rewardCR != null;
    const crSeries = hasCR
      ? linearInterpolateInt(cur.rewardCR!, next.rewardCR!, gap)
      : Array(gap).fill(cur.rewardCR ?? 0);

    // 구간 라벨: 일단 직전 라벨로 채움
    const bracketSeries = Array(gap).fill(cur.bracket ?? "");

    for (let k = 0; k < gap; k++) {
      out.push({
        from: cur.from + k + 1,
        needExp: needSeries[k],
        rewardCR: crSeries[k],
        bracket: bracketSeries[k],
      });
    }
  }

  // maxLevel까지 잘렸다면 끝쪽은 마지막 앵커 트렌드로 연장
  // 여기서는 앵커의 마지막 값만 사용하고 maxLevel까지만 슬라이스
  return out.filter((row) => row.from >= 1 && row.from <= maxLevel).sort((a, b) => a.from - b.from);
}
