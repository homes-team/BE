package com.homes.backend.domain.chat.websocket;

import com.homes.backend.domain.chat.entity.ChatRoom;
import com.homes.backend.domain.chat.exception.ChatErrorCode;
import com.homes.backend.domain.chat.repository.ChatRoomRepository;
import com.homes.backend.global.exception.CustomException;
import com.homes.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * SUBSCRIBE/SEND 시점에 "이 세션의 유저가 진짜 이 채팅방 멤버인지" 검증한다.
 * 멤버인지 판단하는 로직 자체는 REST(ChatService)에서 쓰던 ChatRoom.isMember()를 그대로 재사용한다.
 */
@Component
@RequiredArgsConstructor
public class ChatChannelInterceptor implements ChannelInterceptor {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command)) {
            Long chatId = extractChatId(accessor.getDestination());
            Object userIdAttr = accessor.getSessionAttributes() != null
                    ? accessor.getSessionAttributes().get("userId")
                    : null;

            if (chatId == null || userIdAttr == null) {
                throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
            }

            Long userId = (Long) userIdAttr;
            ChatRoom room = chatRoomRepository.findById(chatId)
                    .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

            if (!room.isMember(userId)) {
                throw new CustomException(ChatErrorCode.NOT_CHAT_MEMBER);
            }
        }

        return message;
    }

    // destination 형식: /topic/chats/{chatId} 또는 /app/chats/{chatId}/send
    private Long extractChatId(String destination) {
        if (destination == null) {
            return null;
        }
        String[] parts = destination.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("chats".equals(parts[i]) && i + 1 < parts.length) {
                try {
                    return Long.parseLong(parts[i + 1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
