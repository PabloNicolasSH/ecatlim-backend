package org.scoutsdecanarias.ecatlim_backend.repository;

import org.scoutsdecanarias.ecatlim_backend.entity.Chat;
import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatOrderByTimestampAsc(Chat to);

    @Query("SELECT m.chat.id, COUNT(m) FROM ChatMessage m " +
            "JOIN m.chat c " +
            "JOIN c.chatMembers u " +
            "WHERE u = :user AND m.isRead = false AND m.from <> :user " +
            "GROUP BY m.chat.id")
    List<Object[]> countUnreadMessagesByChatForUser(@Param("user") User user);
}