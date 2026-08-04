package com.homes.backend.domain.chat.entity;

import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.user.entity.User;
import com.homes.backend.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 집주인(회원) 측

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_user_id", nullable = false)
    private User agentUser; // 중개사 계정의 User (Agent.agent_id가 아니라 User.id를 참조)

    @Column(name = "is_user_left", nullable = false)
    private boolean userLeft = false;

    @Column(name = "is_agent_left", nullable = false)
    private boolean agentLeft = false;

    @Builder
    public ChatRoom(Property property, User user, User agentUser) {
        this.property = property;
        this.user = user;
        this.agentUser = agentUser;
    }

    /**
     * 나갔던 쪽이 있어도 방을 다시 활성 상태로 되돌림
     */
    public void reopen() {
        this.userLeft = false;
        this.agentLeft = false;
    }

    /**
     * callerId가 어느 쪽인지 판별해서 그 쪽만 나간 상태로 표시 (행 삭제 X, 상대방 메시지 이력 보존)
     */
    public void leave(Long callerId) {
        if (this.user.getId().equals(callerId)) {
            this.userLeft = true;
        } else {
            this.agentLeft = true;
        }
    }

    public boolean isMember(Long userId) {
        return this.user.getId().equals(userId) || this.agentUser.getId().equals(userId);
    }
}
