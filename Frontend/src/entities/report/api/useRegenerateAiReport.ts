import { useMutation, useQueryClient } from "@tanstack/react-query";
import { regenerateAiReport } from "./report";

export const useRegenerateAiReport = (sid: number) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => regenerateAiReport(sid),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["report-detail", sid] });
    },
  });
};
