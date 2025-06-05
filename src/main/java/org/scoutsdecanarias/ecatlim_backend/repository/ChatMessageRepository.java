package org.scoutsdecanarias.ecatlim_backend.repository;

import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByFromUserOrToUser(String from, String to);
    List<ChatMessage> findByFromUserAndToUserOrFromUserAndToUser(
            String from1, String to1, String from2, String to2);
}