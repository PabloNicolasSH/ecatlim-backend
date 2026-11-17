package org.scoutsdecanarias.ecatlim_backend.features.chat.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.chat.Chat;
import org.scoutsdecanarias.ecatlim_backend.features.chat.dto.NewChatFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.chat.exception.ChatException;
import org.scoutsdecanarias.ecatlim_backend.features.chat.repository.ChatMessageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.chat.repository.ChatRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.UserService;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceMessageTest {

    @Mock ChatMessageRepository chatMessageRepository;
    @Mock ChatRepository chatRepository;
    @Mock UserService userService;
    @Mock UserRepository userRepository;
    @Mock UserFileService userFileService;

    private ChatService chatService;
    private User sender;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(chatMessageRepository, chatRepository, userService, userRepository, userFileService);
        sender = user(1, "sender@test.com", true);
        lenient().when(userService.getUserByEmail("sender@test.com")).thenReturn(sender);
        lenient().when(chatRepository.existsById(5)).thenReturn(true);
        lenient().when(chatRepository.existsByIdAndMemberEmail(5, "sender@test.com")).thenReturn(true);
        lenient().when(chatRepository.findById(5)).thenReturn(java.util.Optional.of(new Chat()));
        lenient().when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("sender@test.com", null, List.of()));
    }

    @AfterEach
    void logOut() {
        SecurityContextHolder.clearContext();
    }

    private static User user(int id, String email, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setEnabled(enabled);
        return user;
    }

    @Test
    void rejectsEmptyAndTooLongMessages() {
        assertThatThrownBy(() -> chatService.saveChatMessage(5, "sender@test.com", null))
                .isInstanceOf(ChatException.class).extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatThrownBy(() -> chatService.saveChatMessage(5, "sender@test.com", "   "))
                .isInstanceOf(ChatException.class).extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatThrownBy(() -> chatService.saveChatMessage(5, "sender@test.com", "x".repeat(2001)))
                .isInstanceOf(ChatException.class).extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    void aDeactivatedUserCannotSend() {
        sender.setEnabled(false);

        assertThatThrownBy(() -> chatService.saveChatMessage(5, "sender@test.com", "hola"))
                .isInstanceOf(ChatException.class).extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void limitsHowFastAUserCanSend() {
        for (int i = 0; i < 20; i++) {
            chatService.saveChatMessage(5, "sender@test.com", "mensaje " + i);
        }

        assertThatThrownBy(() -> chatService.saveChatMessage(5, "sender@test.com", "uno más"))
                .isInstanceOf(ChatException.class).extracting("status").isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void newChatMembersMustExistBeActiveAndNotRepeatTheCreator() {
        NewChatFormDto missing = new NewChatFormDto("n", "d", List.of(2, 3));
        when(userRepository.findAllById(any())).thenReturn(List.of(user(2, "a@test.com", true)));
        assertThatThrownBy(() -> chatService.saveChat(missing, null))
                .isInstanceOf(EcatlimException.class).extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        NewChatFormDto inactive = new NewChatFormDto("n", "d", List.of(2));
        when(userRepository.findAllById(any())).thenReturn(List.of(user(2, "a@test.com", false)));
        assertThatThrownBy(() -> chatService.saveChat(inactive, null))
                .isInstanceOf(EcatlimException.class).extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        NewChatFormDto onlyMe = new NewChatFormDto("n", "d", List.of(1));
        when(userRepository.findAllById(any())).thenReturn(List.of(sender));
        assertThatThrownBy(() -> chatService.saveChat(onlyMe, null))
                .isInstanceOf(EcatlimException.class).extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void theCreatorIsAddedOnceEvenIfTheyAreInTheList() {
        NewChatFormDto form = new NewChatFormDto("n", "d", List.of(1, 2));
        when(userRepository.findAllById(any())).thenReturn(List.of(sender, user(2, "a@test.com", true)));
        when(chatRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Chat chat = chatService.saveChat(form, null);

        assertThat(chat.getChatMembers()).extracting(User::getId).containsExactlyInAnyOrder(1, 2);
    }
}
