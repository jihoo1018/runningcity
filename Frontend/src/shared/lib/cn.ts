import { clsx } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Tailwind-friendly className 병합 유틸
 * - clsx: 조건부 클래스 결합
 * - twMerge: Tailwind 중복 속성 병합 (bg-*, text-*, p-*, etc.)
 *
 * 사용 예시:
 *   cn("p-2", isActive && "bg-blue-500", className)
 */
export function cn(...inputs: any[]) {
  return twMerge(clsx(inputs));
}
