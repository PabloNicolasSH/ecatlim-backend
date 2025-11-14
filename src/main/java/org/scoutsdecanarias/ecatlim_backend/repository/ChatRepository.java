package org.scoutsdecanarias.ecatlim_backend.repository;

import org.scoutsdecanarias.ecatlim_backend.entity.Chat;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    @Query("""
        SELECT c FROM Chat c
        JOIN c.chatMembers m
        WHERE m.email = :email
    """)
    List<Chat> findAllByMemberEmail(@Param("email") String email);

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Chat c
        JOIN c.chatMembers m
        WHERE c.id = :chatId AND m.email = :email
    """)
    boolean existsByIdAndMemberEmail(@Param("chatId") Integer chatId,
                                     @Param("email") String email);

    Chat getChatById(Integer id);
}