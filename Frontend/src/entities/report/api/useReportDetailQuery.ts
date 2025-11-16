// src/entities/report/api/useReportDetailQuery.ts
import { useQuery } from "@tanstack/react-query";
import { fetchReportDetail } from "./report";

export const useReportDetailQuery = (sid: number) =>
  useQuery({
    queryKey: ["report-detail", sid],
    queryFn: () => fetchReportDetail(sid),
    enabled: Number.isFinite(sid),
  });
