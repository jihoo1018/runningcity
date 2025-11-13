export const FLOWS = {
  setting: ["root", "confirm"] as const,
  entry: ["root", "level"] as const,
} as const;

export type FlowKey = keyof typeof FLOWS;
export type StepKey<F extends FlowKey = FlowKey> = (typeof FLOWS)[F][number];

export type ModalProps = { onClose: () => void };

export type FlowRegistry = {
  [F in FlowKey]: Record<StepKey<F>, React.ComponentType<ModalProps>>;
};
