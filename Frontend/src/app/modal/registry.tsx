import { lazy } from "react";
import type { FlowRegistry } from "./types";

const SettingRoot = lazy(() => import("@/features/modals/setting/Root"));
const SettingConfirm = lazy(() => import("@/features/modals/setting/Confirm"));
const SettingWithdraw = lazy(() => import("@/features/modals/setting/Withdraw"));
const SettingGoal = lazy(() => import("@/features/modals/setting/Goal"));
const AlertRoot = lazy(() => import("@/features/modals/alert/Root"));

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
} satisfies FlowRegistry;
