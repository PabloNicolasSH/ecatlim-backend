package org.scoutsdecanarias.ecatlim_backend.core.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scoutsdecanarias.ecatlim_backend.features.chat.repository.ChatRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StompAuthorizationInterceptorTest {

    private ChatRepository chatRepository;
    private StompAuthorizationInterceptor interceptor;
    private final MessageChannel channel = mock(MessageChannel.class);

    @BeforeEach
    void setUp() {
        chatRepository = mock(ChatRepository.class);
        interceptor = new StompAuthorizationInterceptor(chatRepository);
        when(chatRepository.existsByIdAndMemberEmail(1, "member@test.com")).thenReturn(true);
    }

    private static Message<byte[]> frame(StompCommand command, String destination, String user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        if (user != null) {
            Principal principal = () -> user;
            accessor.setUser(principal);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void memberCanSubscribeToChatTopicsAndSubtopics() {
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/chat/1", "member@test.com"), channel)).isNotNull();
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/chat/1/updated", "member@test.com"), channel)).isNotNull();
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/chat/1/message-deleted", "member@test.com"), channel)).isNotNull();
    }

    @Test
    void nonMemberCannotSubscribeToAnotherChat() {
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/chat/1", "intruder@test.com"), channel)).isNull();
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/chat/2", "member@test.com"), channel)).isNull();
    }

    @Test
    void userDestinationsAreAllowedButRawQueuesAndOtherTopicsAreNot() {
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/user/queue/errors", "member@test.com"), channel)).isNotNull();
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/queue/chat-notifications-userabc123", "member@test.com"), channel)).isNull();
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/chat/*", "member@test.com"), channel)).isNull();
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/anything", "member@test.com"), channel)).isNull();
    }

    @Test
    void clientsCanOnlySendToApplicationDestinations() {
        assertThat(interceptor.preSend(frame(StompCommand.SEND, "/app/chat/1/send", "member@test.com"), channel)).isNotNull();
        assertThat(interceptor.preSend(frame(StompCommand.SEND, "/topic/chat/1", "member@test.com"), channel)).isNull();
        assertThat(interceptor.preSend(frame(StompCommand.SEND, "/queue/errors", "member@test.com"), channel)).isNull();
    }

    @Test
    void framesWithoutUserAreDenied() {
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/user/queue/errors", null), channel)).isNull();
    }

    @Test
    void otherCommandsPassThrough() {
        assertThat(interceptor.preSend(frame(StompCommand.CONNECT, null, null), channel)).isNotNull();
        verifyNoInteractions(chatRepository);
    }
}
