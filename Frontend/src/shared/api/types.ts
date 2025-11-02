// src/shared/api/types.ts

export interface ApiResponse<T> {
  status: number;
  code: string;
  message: string;
  data: T;
  error: {
    details?: Array<{
      field: string;
      message: string;
    }> | null;
  } | null;
}

