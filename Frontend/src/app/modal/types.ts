export const FLOWS = {
  setting: ["root", "confirm", "withdraw", "goal"] as const,
  alert: ["root"] as const,
  user: ["modify", "confirm"] as const,
} as const;

export type FlowKey = keyof typeof FLOWS;
export type StepKey<F extends FlowKey = FlowKey> = (typeof FLOWS)[F][number];

export type ModalProps = {
  onClose: () => void;
  payload?: unknown;
};

export type FlowRegistry = {
  [F in FlowKey]: Record<StepKey<F>, React.ComponentType<ModalProps>>;
};
