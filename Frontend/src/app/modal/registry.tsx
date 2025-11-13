import { lazy } from "react";
import type { FlowRegistry } from "./types";

const SettingsRoot = lazy(() => import("@/features/modals/setting/Root"));
const SettingsConfirm = lazy(() => import("@/features/modals/setting/Confirm"));
const EntryRoot = lazy(() => import("@/features/modals/entry/Root"));
const EntryLevel = lazy(() => import("@/features/modals/entry/Level"));
export const REGISTRY = {
  setting: {
    root: SettingsRoot,
    confirm: SettingsConfirm,
  },
  entry: {
    root: EntryRoot,
    level: EntryLevel,
  },
} satisfies FlowRegistry;
