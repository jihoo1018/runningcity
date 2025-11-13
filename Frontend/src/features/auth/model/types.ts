// src/features/auth/model/types.ts

export type LoginBody = {
  email: string;
  password: string;
};

export type LoginResponse = {
  userId: number;
  userNickname: string;
  userCode: string;
  totalexp: number;
};

