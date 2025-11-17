package com.runningcity.friendship.service;

import com.runningcity.friendship.dto.*;
import com.runningcity.friendship.entity.Friendship;
import com.runningcity.friendship.entity.Friendship.FriendshipStatus;
import com.runningcity.friendship.exception.FriendshipResponseCode;
import com.runningcity.friendship.repository.FriendshipRepository;
import com.runningcity.global.exception.BaseException;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.user.entity.User;
import com.runningcity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * 친구 요청 보내기
     * @param requesterId 요청 보낸 사람 ID
     * @param request 친구 코드가 포함된 요청
     * @return 친구 요청 응답
     */
    @Transactional
    public FriendshipResponse sendFriendRequest(Long requesterId, AddFriendRequest request) {
        // 1. 요청 보낸 사람 조회
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.USER_NOT_FOUND));

        // 2. 친구 코드로 사용자 찾기
        User addressee = userRepository.findByUserCode(request.getFriendCode())
                .orElseThrow(() -> new BaseException(FriendshipResponseCode.USER_NOT_FOUND));

        // 3. 자기 자신에게 요청 방지
        if (requesterId.equals(addressee.getUserId())) {
            throw new BaseException(FriendshipResponseCode.FRIEND_REQUEST_SELF);
        }

        // 4. 이미 친구인지 확인 (양방향 ACCEPTED 관계 체크)
        if (friendshipRepository.existsFriendship(requesterId, addressee.getUserId())) {
            throw new BaseException(FriendshipResponseCode.FRIEND_ALREADY_EXISTS);
        }

        // 5. 이미 존재하는 요청 확인 (같은 방향)
        Optional<Friendship> existing = friendshipRepository
                .findByRequesterUserIdAndAddresseeUserId(requesterId, addressee.getUserId());

        if (existing.isPresent()) {
            Friendship existingFriendship = existing.get();
            if (existingFriendship.getStatus() == FriendshipStatus.PENDING) {
                throw new BaseException(FriendshipResponseCode.FRIEND_REQUEST_ALREADY_SENT);
            } else if (existingFriendship.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new BaseException(FriendshipResponseCode.FRIEND_ALREADY_EXISTS);
            }
        }

        // 6. 반대 방향 PENDING 요청 확인 (양방향 요청 체크)
        Optional<Friendship> oppositeRequest = friendshipRepository
                .findByRequesterUserIdAndAddresseeUserId(addressee.getUserId(), requesterId);

        if (oppositeRequest.isPresent()) {
            Friendship opposite = oppositeRequest.get();
            if (opposite.getStatus() == FriendshipStatus.PENDING) {
                // 양방향 요청이면 기존 요청만 ACCEPTED로 변경 (중복 레코드 방지)
                opposite.accept();
                return FriendshipResponse.from(friendshipRepository.save(opposite));
            } else if (opposite.getStatus() == FriendshipStatus.ACCEPTED) {
                // 반대 방향에 이미 ACCEPTED가 있으면 에러
                throw new BaseException(FriendshipResponseCode.FRIEND_ALREADY_EXISTS);
            }
        }

        // 7. 일반적인 경우: PENDING 요청 생성
        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(FriendshipStatus.PENDING)
                .build();

        return FriendshipResponse.from(friendshipRepository.save(friendship));
    }

    /**
     * 내 코드 조회
     * @param userId 사용자 ID
     * @return 내 코드
     */
    public GetMyCodeResponse getMyCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.USER_NOT_FOUND));

        return GetMyCodeResponse.builder()
                .userCode(user.getUserCode())
                .build();
    }

    /**
     * 보낸 요청 목록 조회
     * @param userId 사용자 ID
     * @return 보낸 요청 목록
     */
    public List<GetSentRequestsResponse> getSentRequests(Long userId) {
        List<Friendship> friendships = friendshipRepository
                .findSentRequestsByUserIdAndStatus(userId, FriendshipStatus.PENDING);

        return friendships.stream()
                .map(GetSentRequestsResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 받은 요청 목록 조회
     * @param userId 사용자 ID
     * @return 받은 요청 목록
     */
    public List<GetReceivedRequestsResponse> getReceivedRequests(Long userId) {
        List<Friendship> friendships = friendshipRepository
                .findReceivedRequestsByUserIdAndStatus(userId, FriendshipStatus.PENDING);

        return friendships.stream()
                .map(GetReceivedRequestsResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 친구 요청 취소
     * @param userId 사용자 ID
     * @param friendshipId 친구 요청 ID
     */
    @Transactional
    public void cancelFriendRequest(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND, "친구 요청을 찾을 수 없습니다."));

        // 요청 보낸 사람만 취소 가능
        if (!friendship.getRequester().getUserId().equals(userId)) {
            throw new BaseException(CommonResponseCode.FORBIDDEN, "친구 요청을 취소할 권한이 없습니다.");
        }

        // PENDING 상태만 취소 가능
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BaseException(CommonResponseCode.BAD_REQUEST, "취소할 수 없는 요청입니다.");
        }

        // 레코드 삭제
        friendshipRepository.delete(friendship);
    }

    /**
     * 친구 요청 수락
     * @param userId 사용자 ID
     * @param friendshipId 친구 요청 ID
     * @return 수락된 친구 요청 응답
     */
    @Transactional
    public FriendshipResponse acceptFriendRequest(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND, "친구 요청을 찾을 수 없습니다."));

        // 요청 받은 사람만 수락 가능
        if (!friendship.getAddressee().getUserId().equals(userId)) {
            throw new BaseException(CommonResponseCode.FORBIDDEN, "친구 요청을 수락할 권한이 없습니다.");
        }

        // PENDING 상태만 수락 가능
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BaseException(CommonResponseCode.BAD_REQUEST, "수락할 수 없는 요청입니다.");
        }

        friendship.accept();
        return FriendshipResponse.from(friendshipRepository.save(friendship));
    }

    /**
     * 친구 요청 거절
     * @param userId 사용자 ID
     * @param friendshipId 친구 요청 ID
     */
    @Transactional
    public void rejectFriendRequest(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND, "친구 요청을 찾을 수 없습니다."));

        // 요청 받은 사람만 거절 가능
        if (!friendship.getAddressee().getUserId().equals(userId)) {
            throw new BaseException(CommonResponseCode.FORBIDDEN, "친구 요청을 거절할 권한이 없습니다.");
        }

        // PENDING 상태만 거절 가능
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BaseException(CommonResponseCode.BAD_REQUEST, "거절할 수 없는 요청입니다.");
        }

        // 레코드 삭제
        friendshipRepository.delete(friendship);
    }

    /**
     * 친구 삭제 (양방향 모두 삭제)
     * @param userId 사용자 ID
     * @param friendId 친구 ID
     */
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        // 양방향 친구 관계 조회
        List<Friendship> friendships = friendshipRepository.findFriendshipBetweenUsers(userId, friendId);

        if (friendships.isEmpty()) {
            throw new BaseException(CommonResponseCode.NOT_FOUND, "친구 관계를 찾을 수 없습니다.");
        }

        // 양방향 레코드 모두 삭제
        friendshipRepository.deleteAll(friendships);
    }

    /**
     * 친구 목록 조회 (랭킹 시스템, total_exp 기준 내림차순)
     * @param userId 사용자 ID
     * @return 친구 랭킹 목록 (현재 사용자 포함)
     */
    public List<FriendRankingResponse> getFriendRankingList(Long userId) {
        // 1. 현재 사용자 조회
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.USER_NOT_FOUND));

        // 2. 친구 목록 조회 (total_exp 기준 내림차순)
        List<User> friends = friendshipRepository.findFriendsByUserIdOrderByTotalExp(userId);

        // 3. 현재 사용자도 포함하여 리스트 생성
        List<User> allUsers = new ArrayList<>(friends);
        allUsers.add(currentUser);

        // 4. total_exp 기준 내림차순 정렬 (동일한 total_exp일 경우 현재 사용자를 먼저)
        allUsers.sort(Comparator.comparing(User::getTotalExp).reversed()
                .thenComparing((u1, u2) -> {
                    // 동일한 total_exp일 경우, 현재 사용자를 먼저 보여줌
                    boolean u1IsMe = u1.getUserId().equals(userId);
                    boolean u2IsMe = u2.getUserId().equals(userId);
                    if (u1IsMe && !u2IsMe) return -1;  // u1이 현재 사용자면 앞으로
                    if (!u1IsMe && u2IsMe) return 1;   // u2가 현재 사용자면 앞으로
                    return u1.getUserId().compareTo(u2.getUserId()); // 둘 다 아니면 userId 오름차순
                }));

        // 5. 순위 계산 및 DTO 변환
        List<FriendRankingResponse> rankingList = new ArrayList<>();
        int rank = 1;
        Long previousExp = null;

        for (int i = 0; i < allUsers.size(); i++) {
            User user = allUsers.get(i);
            
            // 동일한 total_exp가 아닌 경우에만 순위 증가
            if (previousExp != null && !user.getTotalExp().equals(previousExp)) {
                rank = i + 1;
            }
            // 첫 번째 사용자는 항상 1등
            else if (previousExp == null) {
                rank = 1;
            }
            // 동일한 total_exp인 경우 같은 순위 유지 (rank는 변경하지 않음)

            boolean isMe = user.getUserId().equals(userId);
            rankingList.add(FriendRankingResponse.from(user, rank, isMe));

            previousExp = user.getTotalExp();
        }

        return rankingList;
    }
}

