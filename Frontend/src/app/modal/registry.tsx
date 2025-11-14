import { lazy } from "react";
import type { FlowRegistry } from "./types";

const SettingRoot = lazy(() => import("@/features/modals/setting/Root"));
const SettingConfirm = lazy(() => import("@/features/modals/setting/Confirm"));
const SettingWithdraw = lazy(() => import("@/features/modals/setting/Withdraw"));
const SettingGoal = lazy(() => import("@/features/modals/setting/Goal"));
const AlertRoot = lazy(() => import("@/features/modals/alert/Root"));
const UserModify = lazy(() => import("@/features/modals/user/Modify"));
const UserConfirm = lazy(() => import("@/features/modals/user/Confirm"));
const MissionRoot = lazy(() => import("@/features/modals/mission/Root"));
const MissionConfirm = lazy(() => import("@/features/modals/mission/Confirm"));

export const REGISTRY = {
  setting: {
    root: SettingRoot,
    confirm: SettingConfirm,
    withdraw: SettingWithdraw,
    goal: SettingGoal,
  },
  alert: {
    root: AlertRoot,
  },
  user: {
    modify: UserModify,
    confirm: UserConfirm,
  },
  mission: {
    root: MissionRoot,
    confirm: MissionConfirm,
  },
} satisfies FlowRegistry;
