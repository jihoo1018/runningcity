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

export type SignupBody = {
  email: string;
  password: string;
  passwordConfirm: string;
};

export type CheckEmailResponse = {
  exists: boolean;
};

