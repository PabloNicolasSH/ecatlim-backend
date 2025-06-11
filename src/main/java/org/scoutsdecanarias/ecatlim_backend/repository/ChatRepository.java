package org.scoutsdecanarias.ecatlim_backend.repository;

import org.scoutsdecanarias.ecatlim_backend.entity.Chat;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    Chat getChatById(Integer id);

    List<Chat> findByChatMembersContains(List<User> chatMembers);
}
