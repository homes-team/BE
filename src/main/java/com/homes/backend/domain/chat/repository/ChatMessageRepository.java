package com.homes.backend.domain.chat.repository;

import com.homes.backend.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // createdAt만으로는 동일 시각에 온 메시지의 순서가 불안정할 수 있어 id를 2차 정렬 키로 추가
    List<ChatMessage> findAllByRoomIdOrderByCreatedAtAscIdAsc(Long roomId);
}
