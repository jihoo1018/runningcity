import { LEVEL_ANCHORS, MAX_LEVEL } from "@/shared/config/levels";
import { expandAnchors } from "@/shared/lib/progression";

export type LevelRow = {
  from: number; // L → L+1
  needExp: number; // 그 레벨업에 필요한 EXP
  rewardCR: number; // 레벨업 보상
  bracket: string; // 구간 라벨
  cumExpToNext: number; // L+1 도달까지 누적 요구치 (누적합)
};

let CACHED_TABLE: LevelRow[] | null = null;

export function getLevelTable(maxLevel: number = MAX_LEVEL): LevelRow[] {
  if (CACHED_TABLE && CACHED_TABLE.at(-1)?.from === maxLevel) return CACHED_TABLE;

  const rows = expandAnchors(LEVEL_ANCHORS, maxLevel);

  // 누적 합 계산
  let acc = 0;
  const table: LevelRow[] = rows.map((r) => {
    acc += r.needExp;
    return {
      from: r.from,
      needExp: r.needExp,
      rewardCR: r.rewardCR,
      bracket: r.bracket,
      cumExpToNext: acc, // 1→2부터 누적
    };
  });

  CACHED_TABLE = table;
  return table;
}

export type LevelInfo = {
  level: number; // 현재 레벨 (예: 17)
  expIntoLevel: number; // 현재 레벨에 들어온 EXP
  needForNext: number; // 다음 레벨까지 필요한 총 EXP
  progress: number; // 0~1
  nextRewardCR: number; // 다음 레벨업 보상
  bracket: string; // 현재 구간 라벨
  // 선택: 다음 레벨이 없으면(만렙) null
  nextLevel: number | null;
};

export function expToLevel(totalExp: number, maxLevel: number = MAX_LEVEL): number {
  if (totalExp <= 0) return 1;
  const table = getLevelTable(maxLevel);
  // 누적 요구치를 넘어서는 가장 큰 from를 찾는다
  for (let i = 0; i < table.length; i++) {
    if (totalExp < table[i].cumExpToNext) {
      return table[i].from; // 아직 L→L+1 구간 내
    }
  }
  // 누적을 다 넘었으면 만렙
  return maxLevel + 1; // (표현상: 50→51 못 가니 51로 표기, UI서 "만렙" 처리)
}

export function getLevelInfo(totalExp: number, maxLevel: number = MAX_LEVEL): LevelInfo {
  const table = getLevelTable(maxLevel);

  if (totalExp <= 0) {
    const first = table[0];
    return {
      level: 1,
      expIntoLevel: 0,
      needForNext: first.needExp,
      progress: 0,
      nextRewardCR: first.rewardCR,
      bracket: first.bracket,
      nextLevel: 2,
    };
  }

  let prevCum = 0;
  for (let i = 0; i < table.length; i++) {
    const row = table[i];
    if (totalExp < row.cumExpToNext) {
      const expInto = totalExp - prevCum;
      const progress = Math.max(0, Math.min(1, expInto / row.needExp));
      return {
        level: row.from,
        expIntoLevel: expInto,
        needForNext: row.needExp,
        progress,
        nextRewardCR: row.rewardCR,
        bracket: row.bracket,
        nextLevel: row.from + 1,
      };
    }
    prevCum = row.cumExpToNext;
  }

  // 만렙
  const last = table.at(-1)!;
  return {
    level: maxLevel + 1, // UI에서는 "Lv.50 (MAX)"처럼 렌더
    expIntoLevel: last.needExp,
    needForNext: 0,
    progress: 1,
    nextRewardCR: 0,
    bracket: last.bracket,
    nextLevel: null,
  };
}
