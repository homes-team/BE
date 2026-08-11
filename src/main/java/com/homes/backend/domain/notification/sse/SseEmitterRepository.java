package com.homes.backend.domain.notification.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 유저별로 열려있는 SSE 연결(여러 탭/기기 동시 접속 가능)을 메모리에 들고 있는 저장소.
 * 서버 인스턴스가 여러 대로 늘어나면(수평 확장) 이 방식으론 부족해지고 Redis Pub/Sub 등이 필요하지만,
 * 지금 규모에서는 과한 설계라 인메모리로 충분하다.
 */
@Component
public class SseEmitterRepository {

    private final Map<Long, List<SseEmitter>> emittersByUserId = new ConcurrentHashMap<>();

    public void save(Long userId, SseEmitter emitter) {
        emittersByUserId.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void remove(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUserId.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
        }
    }

    public List<SseEmitter> findByUserId(Long userId) {
        return emittersByUserId.getOrDefault(userId, List.of());
    }
}
