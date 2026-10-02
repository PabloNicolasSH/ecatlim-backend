package org.scoutsdecanarias.ecatlim_backend.features.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.chat.enums.ChatMessageType;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User from;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(length = 2000)
    private String message;

    private ZonedDateTime timestamp;

    private ZonedDateTime asReadAt;

    private boolean isRead = false;

    private boolean isEdited = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private ChatMessageType type = ChatMessageType.TEXT;
}