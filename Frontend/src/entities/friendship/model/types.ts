// src/entities/friendship/model/types.ts

export type FriendRankingItem = {
  rank: number;
  userId: number;
  nickname: string;
  profileImageUrl: string;
  level: number;
  totalExp: number;
  isMe: boolean;
};

export type FriendRankingResponse = FriendRankingItem[];

export type MyCodeResponse = {
  userCode: string;
};

export type SentRequestItem = {
  friendshipId: number;
  friendId: number;
  friendNickname: string;
  friendProfileImageUrl: string;
  friendLevel: number;
  createdAt: string;
};

export type SentRequestResponse = SentRequestItem[];

export type SendFriendRequestBody = {
  friendCode: string;
};

export type SendFriendRequestResponse = {
  friendshipId: number;
  requesterId: number;
  addresseeId: number;
  status: string;
  createdAt: string;
};

export type ReceivedRequestItem = {
  friendshipId: number;
  requesterId: number;
  requesterNickname: string;
  requesterProfileImageUrl: string;
  requesterLevel: number;
  createdAt: string;
};

export type ReceivedRequestResponse = ReceivedRequestItem[];

export type AcceptFriendRequestResponse = {
  friendshipId: number;
  requesterId: number;
  addresseeId: number;
  status: string;
  createdAt: string;
};

