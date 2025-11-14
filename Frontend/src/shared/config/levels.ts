export type LevelAnchor = {
  from: number; // L → L+1 (예: 1이면 1→2)
  needExp: number; // 해당 레벨업에 필요한 EXP
  rewardCR?: number; // 레벨업 보상 CR (있으면 사용, 없으면 보간)
  bracket?: string; // 구간 라벨
};

export const LEVEL_ANCHORS: LevelAnchor[] = [
  { from: 1, needExp: 30, rewardCR: 90, bracket: "극초반" },
  { from: 2, needExp: 60, rewardCR: 110, bracket: "극초반" },
  { from: 3, needExp: 90, rewardCR: 130, bracket: "극초반" },
  { from: 4, needExp: 120, rewardCR: 150, bracket: "극초반" },
  { from: 5, needExp: 150, rewardCR: 170, bracket: "극초반" },
  { from: 6, needExp: 200, rewardCR: 190, bracket: "초반" },
  { from: 7, needExp: 250, rewardCR: 210, bracket: "초반" },
  { from: 8, needExp: 300, rewardCR: 230, bracket: "초반" },
  { from: 9, needExp: 350, rewardCR: 250, bracket: "초반" },
  { from: 10, needExp: 400, rewardCR: 270, bracket: "초반" },

  { from: 11, needExp: 3662, rewardCR: 270, bracket: "중반 전환" },
  { from: 12, needExp: 4217, rewardCR: 290, bracket: "중반" },
  { from: 13, needExp: 4820, rewardCR: 310, bracket: "중반" },
  { from: 14, needExp: 5471, rewardCR: 330, bracket: "중반" },
  { from: 15, needExp: 6168, rewardCR: 350, bracket: "중반" },
  { from: 16, needExp: 6912, rewardCR: 370, bracket: "중반" },
  { from: 17, needExp: 7703, rewardCR: 390, bracket: "중반" },
  { from: 18, needExp: 8540, rewardCR: 410, bracket: "중반" },
  { from: 19, needExp: 9423, rewardCR: 430, bracket: "중반" },
  { from: 20, needExp: 10351, rewardCR: 450, bracket: "중반" },

  { from: 25, needExp: 15625, rewardCR: 550, bracket: "중후반" },
  { from: 30, needExp: 22136, rewardCR: 650, bracket: "후반" },
  { from: 35, needExp: 29767, rewardCR: 750, bracket: "후반" },
  { from: 40, needExp: 38487, rewardCR: 850, bracket: "후반" },
  { from: 45, needExp: 48274, rewardCR: 950, bracket: "후반" },
  { from: 50, needExp: 59112, rewardCR: 1050, bracket: "후반" },
];

export const MAX_LEVEL = 50;
