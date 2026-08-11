package com.homes.backend.domain.notification.service;

import com.homes.backend.domain.notification.dto.response.NotificationResDto;
import com.homes.backend.domain.notification.entity.Notification;
import com.homes.backend.domain.notification.entity.NotificationType;
import com.homes.backend.domain.notification.exception.NotificationErrorCode;
import com.homes.backend.domain.notification.repository.NotificationRepository;
import com.homes.backend.domain.notification.sse.SseEmitterRepository;
import com.homes.backend.domain.user.entity.User;
import com.homes.backend.domain.user.exception.UserErrorCode;
import com.homes.backend.domain.user.repository.UserRepository;
import com.homes.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final long SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L; // 30분

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseEmitterRepository sseEmitterRepository;

    public List<NotificationResDto> getMyNotifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResDto::from)
                .toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /**
     * 채팅 메시지 도착, 매칭 확정 등 다른 도메인 서비스에서 호출해서 알림을 생성 + 실시간 push한다.
     */
    @Transactional
    public void createNotification(Long userId, NotificationType type, String content, Long referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Notification notification = Notification.builder()
                .content(content)
                .user(user)
                .type(type)
                .referenceId(referenceId)
                .build();

        notificationRepository.save(notification);
        push(userId, NotificationResDto.from(notification));
    }

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        sseEmitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> sseEmitterRepository.remove(userId, emitter));
        emitter.onTimeout(() -> sseEmitterRepository.remove(userId, emitter));
        emitter.onError(e -> sseEmitterRepository.remove(userId, emitter));

        try {
            // 연결 직후 더미 이벤트를 보내지 않으면 일부 프록시/브라우저가 응답 없는 연결로 보고 바로 끊어버림
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            sseEmitterRepository.remove(userId, emitter);
        }

        return emitter;
    }

    private void push(Long userId, NotificationResDto notification) {
        for (SseEmitter emitter : sseEmitterRepository.findByUserId(userId)) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification));
            } catch (IOException e) {
                log.warn("SSE 전송 실패, 연결 제거: userId={}", userId);
                sseEmitterRepository.remove(userId, emitter);
            }
        }
    }
}
