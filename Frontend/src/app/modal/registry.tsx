import { lazy } from "react";
import type { FlowRegistry } from "./types";

const SettingsRoot = lazy(() => import("@/features/modals/setting/Root"));
const SettingsConfirm = lazy(() => import("@/features/modals/setting/Confirm"));

export const REGISTRY = {
  setting: {
    root: SettingsRoot,
    confirm: SettingsConfirm,
  },
} satisfies FlowRegistry;
