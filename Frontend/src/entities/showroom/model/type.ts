// src/entities/showroom/model/types.ts
export type ApiResponse<T> = {
  status: number;
  code: string;
  message: string;
  data: T;
  error: unknown | null;
};
