package org.scoutsdecanarias.ecatlim_backend.features.chat.exception;

import lombok.Getter;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.springframework.http.HttpStatus;

@Getter
public class ChatException extends EcatlimException {
    private final ChatErrorCode code;
    private final Integer chatId;

    public ChatException(ChatErrorCode code, Integer chatId, String message, HttpStatus status) {
        super(message, status);
        this.code = code;
        this.chatId = chatId;
    }

    public static ChatException chatNotFound(Integer chatId) {
        return new ChatException(ChatErrorCode.CHAT_NOT_FOUND, chatId, "El chat ya no existe", HttpStatus.NOT_FOUND);
    }

    public static ChatException notAMember(Integer chatId) {
        return new ChatException(ChatErrorCode.NOT_A_MEMBER, chatId, "Ya no perteneces a este chat", HttpStatus.FORBIDDEN);
    }

    public static ChatException messageNotFound(Integer chatId) {
        return new ChatException(ChatErrorCode.MESSAGE_NOT_FOUND, chatId, "El mensaje ya no existe", HttpStatus.NOT_FOUND);
    }

    public static ChatException messageNotDeletable(Integer chatId) {
        return new ChatException(ChatErrorCode.MESSAGE_NOT_DELETABLE, chatId, "No puedes eliminar este mensaje", HttpStatus.FORBIDDEN);
    }

    public static ChatException messageDeletionExpired(Integer chatId, long minutes) {
        return new ChatException(ChatErrorCode.MESSAGE_NOT_DELETABLE, chatId,
                "Solo puedes eliminar mensajes enviados en los últimos " + minutes + " minutos", HttpStatus.FORBIDDEN);
    }

    public static ChatException unknown(Integer chatId) {
        return new ChatException(ChatErrorCode.UNKNOWN, chatId, "No se ha podido enviar el mensaje", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
