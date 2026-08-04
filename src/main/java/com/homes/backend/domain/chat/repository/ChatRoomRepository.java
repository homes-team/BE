package com.homes.backend.domain.chat.repository;

import com.homes.backend.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * (매물, 회원, 중개사) 조합으로 이미 만들어진 채팅방이 있는지 조회 - 중복 생성 방지용
     */
    Optional<ChatRoom> findByPropertyIdAndUserIdAndAgentUserId(Long propertyId, Long userId, Long agentUserId);

    /**
     * 내 채팅방 목록 - 내가 회원 쪽이든 중개사 쪽이든 조회되지만, 내가 이미 나간 방은 내 목록에서 제외한다.
     * (상대방이 나갔는지 여부와는 무관하게, 내가 안 나갔으면 계속 보임)
     */
    @Query("SELECT r FROM ChatRoom r WHERE " +
            "(r.user.id = :userId AND r.userLeft = false) OR " +
            "(r.agentUser.id = :userId AND r.agentLeft = false)")
    List<ChatRoom> findActiveRoomsByParticipantId(@Param("userId") Long userId);
}
