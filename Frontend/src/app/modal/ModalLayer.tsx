import { Suspense, useEffect } from "react";
import { createPortal } from "react-dom";
import { Navigate, useLocation, useNavigate, useParams } from "react-router-dom";
import { REGISTRY } from "./registry";
import type { FlowKey, StepKey } from "./types";

export function ModalLayer() {
  const { flow, step } = useParams<{ flow: FlowKey; step: StepKey }>();
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as any;
  const background = state?.background;
  const stack = (state?.stack as Array<{ flow: FlowKey; step: StepKey }>) ?? [];

  if (!flow || !step) return null;

  const Flow = REGISTRY[flow];
  const Comp = Flow?.[step as StepKey<typeof flow>];
  if (!Comp) return null;

  if (!background) return <Navigate to="/" replace />;

  useEffect(() => {
    const onPoP = (e: PopStateEvent) => {
      navigate(location, { replace: true, state });
    };
    window.addEventListener("popstate", onPoP);
    return () => window.removeEventListener("popstate", onPoP);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [navigate, location.key]);

  const handleClose = () => navigate(background, { replace: true });

  const node = (
    <div
      className="fixed inset-0 z-[100] grid place-items-center bg-black/50 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
    >
      <Suspense fallback={null}>
        <Comp onClose={handleClose} />
      </Suspense>
    </div>
  );

  const target = document.getElementById("modal-root") ?? document.body;
  return createPortal(node, target);
}
