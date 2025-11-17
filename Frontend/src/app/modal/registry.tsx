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
const BoutiquePurchaseConfirm = lazy(() => import("@/features/modals/boutique/PurchaseConfirm"));
const BoutiqueGachaResult = lazy(() => import("@/features/modals/boutique/GachaResult"));
const MissionRoot = lazy(() => import("@/features/modals/mission/Root"));
const MissionConfirm = lazy(() => import("@/features/modals/mission/Confirm"));
const PrivacySettingRoot = lazy(() => import("@/features/modals/privacySetting/Root"));
const PrivacySettingConfirm = lazy(() => import("@/features/modals/privacySetting/Confirm"));
const PrivacySettingAlert = lazy(() => import("@/features/modals/privacySetting/Alert"));
const CommonFail = lazy(() => import("@/features/modals/common/Fail"));
const SettingTutorial = lazy(() => import("@/features/modals/tutorial"));

export const REGISTRY = {
  setting: {
    root: SettingRoot,
    confirm: SettingConfirm,
    withdraw: SettingWithdraw,
    goal: SettingGoal,
    tutorial: SettingTutorial,
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
  boutique: {
    purchaseConfirm: BoutiquePurchaseConfirm,
    gachaResult: BoutiqueGachaResult,
  },
  privacySetting: {
    root: PrivacySettingRoot,
    confirm: PrivacySettingConfirm,
    alert: PrivacySettingAlert,
  },
  common: {
    fail: CommonFail,
  },
} satisfies FlowRegistry;
