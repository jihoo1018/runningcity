import { lazy } from "react";
import type { FlowRegistry } from "./types";

const SettingsRoot = lazy(() => import("@/features/modals/setting/Root"));
const SettingsConfirm = lazy(() => import("@/features/modals/setting/Confirm"));
const EntryRoot = lazy(() => import("@/features/modals/entry/Root"));
const EntryLevel = lazy(() => import("@/features/modals/entry/Level"));
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
  entry: {
    root: EntryRoot,
    level: EntryLevel,
  },
  mission: {
    root: MissionRoot,
    confirm: MissionConfirm,
  },
} satisfies FlowRegistgry;
