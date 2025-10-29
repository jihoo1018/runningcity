// src/types/mission.ts
export type MissionModalData = {
  id: number;
  date: string;
  serverTime: string;
  targetKm: number;
  currentKm: number;
  progressPercent: number;
  completed: boolean;
  claimed: boolean;
  rewardCoins: number;
};

export type ClaimResponse = {
  rewardCoins: number;
  claimed: boolean;
};
